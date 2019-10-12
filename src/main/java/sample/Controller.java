package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;
import oshi.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Controller {
    SystemInfo si = new SystemInfo();
    HardwareAbstractionLayer hal = si.getHardware();
    OperatingSystem os = si.getOperatingSystem();
    List list = new ArrayList<String>();

    @FXML
    public Label category;
    @FXML
    public Button cpuBtn;
    @FXML
    public Button sensorBtn;
    @FXML
    public Button diskBtn;
    @FXML
    public ListView<String> listView;


    public void getCpu() {
        listView.setItems(null);
        list.clear();
        category.setText("Procesor");
        printProcessor(hal.getProcessor());
    }

    public void getSensor() {
        listView.setItems(null);
        list.clear();
        category.setText("Czujniki");
        printSensor(hal.getSensors());
    }

    public void getDisk() {
        listView.setItems(null);
        list.clear();
        category.setText("Dyski");
        printDisk(hal.getDiskStores());
    }

    public void printProcessor(CentralProcessor processor) {
        list.add(processor.toString());
        list.add("Context Switches/Interrupts: " + processor.getContextSwitches() + " / " + processor.getInterrupts());

        long[] prevTicks = processor.getSystemCpuLoadTicks();
        long[][] prevProcTicks = processor.getProcessorCpuLoadTicks();
        list.add("CPU, IOWait, and IRQ ticks @ 0 sec:" + Arrays.toString(prevTicks));
        // Wait a second...
        Util.sleep(1000);
        long[] ticks = processor.getSystemCpuLoadTicks();
        list.add("CPU, IOWait, and IRQ ticks @ 1 sec:" + Arrays.toString(ticks));
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long sys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;

        list.add(String.format(
                "User: %.1f%% Nice: %.1f%% System: %.1f%% Idle: %.1f%% IOwait: %.1f%% IRQ: %.1f%% SoftIRQ: %.1f%% Steal: %.1f%%",
                100d * user / totalCpu, 100d * nice / totalCpu, 100d * sys / totalCpu, 100d * idle / totalCpu,
                100d * iowait / totalCpu, 100d * irq / totalCpu, 100d * softirq / totalCpu, 100d * steal / totalCpu));
        list.add(String.format("CPU load: %.1f%%", processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100));
        double[] loadAverage = processor.getSystemLoadAverage(3);
        list.add("CPU load averages:" + (loadAverage[0] < 0 ? " N/A" : String.format(" %.2f", loadAverage[0]))
                + (loadAverage[1] < 0 ? " N/A" : String.format(" %.2f", loadAverage[1]))
                + (loadAverage[2] < 0 ? " N/A" : String.format(" %.2f", loadAverage[2])));
        // per core CPU
        StringBuilder procCpu = new StringBuilder("CPU load per processor:");
        double[] load = processor.getProcessorCpuLoadBetweenTicks(prevProcTicks);
        for (double avg : load) {
            procCpu.append(String.format(" %.1f%%", avg * 100));
        }
        list.add(procCpu.toString());
        long freq = processor.getVendorFreq();
        if (freq > 0) {
            list.add("Vendor Frequency: " + FormatUtil.formatHertz(freq));
        }
        freq = processor.getMaxFreq();
        if (freq > 0) {
            list.add("Max Frequency: " + FormatUtil.formatHertz(freq));
        }
        long[] freqs = processor.getCurrentFreq();
        if (freqs[0] > 0) {
            StringBuilder sb = new StringBuilder("Current Frequencies: ");
            for (int i = 0; i < freqs.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(FormatUtil.formatHertz(freqs[i]));
            }
            list.add(sb.toString());
        }
        ObservableList<String> observableArrayList =
                FXCollections.observableArrayList(list);
        listView.setItems(observableArrayList);
    }

    public void printDisk(HWDiskStore[] diskStores) {
        for (HWDiskStore disk : diskStores) {
            list.add(" Nazwa: " + disk.getName());
            list.add(" Model: " + disk.getModel());
            list.add(" Numer seryjny: " + disk.getSerial());
            list.add(" Rozmiar dysku: " + (disk.getSize()/1073741824) + "GB");
            list.add(" Czas pracy: " + (disk.getTransferTime()/60000) + "min");
            list.add(" Czas pracy od znacznika czasu: " + (disk.getTimeStamp()/1440000) + " dni");
            list.add(" Partycje: ");
            HWPartition[] partitions = disk.getPartitions();
            for (HWPartition part : partitions) {
                list.add("     " + part.getIdentification());
                if(part.getMountPoint().length()!=0) {
                    list.add("     " + part.getMountPoint());
                }
                list.add("     " + part.getName());
                list.add("     " + part.getType());
                list.add("     " + (part.getSize()/1073741824) + "GB");
                list.add("");
            }
            list.add("");
        }
        ObservableList<String> observableArrayList =
                FXCollections.observableArrayList(list);
        listView.setItems(observableArrayList);
    }

    public void printSensor(Sensors sensors) {
        if(sensors.getCpuTemperature()!=0) {
            list.add("CPU temperature: " + sensors.getCpuTemperature());
        } else {
            list.add("CPU temperature: not found");
        }
        if(sensors.getCpuVoltage()!=0) {
            list.add("CPU voltage: " + sensors.getCpuVoltage());
        } else {
            list.add("CPU voltage: not found");
        }
        if(sensors.getFanSpeeds().length>0) {
            int[] speeds = sensors.getFanSpeeds();
            for(int i=0;i<speeds.length;i++) {
                list.add("Fan " + i + " speed: " + speeds[i] + "rpm");
            }
        } else {
            list.add("Fan speed: not found");
        }
        ObservableList<String> observableArrayList =
                FXCollections.observableArrayList(list);
        listView.setItems(observableArrayList);
    }
}
