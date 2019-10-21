package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;
import oshi.util.Util;

import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    SystemInfo si = new SystemInfo();
    HardwareAbstractionLayer hal = si.getHardware();
    OperatingSystem os = si.getOperatingSystem();
    List list = new ArrayList<String>();

    @FXML
    public Label category;
    @FXML
    public Button cpuBtn;
    @FXML
    public Button moboBtn;
    @FXML
    public Button sensorBtn;
    @FXML
    public Button diskBtn;
    @FXML
    public Button systemBtn;
    @FXML
    public Button processesBtn;
    @FXML
    public Button usbBtn;
    @FXML
    public Button displayBtn;
    @FXML
    public Button soundCardBtn;
    @FXML
    public Button fileSystemBtn;
    @FXML
    public Button powerSourcesBtn;
    @FXML
    public ListView<String> listView;


    public void getCpu() {
        listView.setItems(null);
        list.clear();
        category.setText("Procesor");
        printProcessor(hal.getProcessor());
    }

    public void getMobo() {
        listView.setItems(null);
        list.clear();
        category.setText("Płyta główna");
        printMobo(hal.getComputerSystem());
    }

    public void getSystem() {
        listView.setItems(null);
        list.clear();
        category.setText("System");
        printSystem(os);
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

    public void getProcesses() {
        listView.setItems(null);
        list.clear();
        category.setText("Procesy");
        printProcesses(os, hal.getMemory());
    }

    public void getUsbDevices() {
        listView.setItems(null);
        list.clear();
        category.setText("Urządzenia USB");
        printUsbDevices(hal.getUsbDevices(true));
    }

    public void getDisplays() {
        listView.setItems(null);
        list.clear();
        category.setText("Monitory");
        printDisplays(hal.getDisplays());
    }

    public void getSoundCards() {
        listView.setItems(null);
        list.clear();
        category.setText("Karty dźwiękowe");
        printSoundCards(hal.getSoundCards());
    }

    public void getFileSystem() {
        listView.setItems(null);
        list.clear();
        category.setText("System plików");
        printFileSystem(os.getFileSystem());
    }

    public void getPowerSources() {
        printPowerSources(hal.getPowerSources());
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
        if (sensors.getCpuTemperature() != 0) {
            list.add("CPU temperature: " + sensors.getCpuTemperature());
        }
        if (sensors.getCpuVoltage() != 0) {
            list.add("CPU voltage: " + sensors.getCpuVoltage());
        }
        if (sensors.getFanSpeeds().length > 0) {
            int[] speeds = sensors.getFanSpeeds();
            for (int i = 0; i < speeds.length; i++) {
                list.add("Fan " + i + " speed: " + speeds[i] + "rpm");
            }
            ObservableList<String> observableArrayList =
                    FXCollections.observableArrayList(list);
            listView.setItems(observableArrayList);
        }
    }
    public void printMobo(ComputerSystem computerSystem) {
        list.add("Płyta główna: " + computerSystem.getBaseboard().getModel());
        list.add(" Dystrybucja: " + computerSystem.getBaseboard().getManufacturer());
        list.add(" Numer seryjny: " + computerSystem.getBaseboard().getSerialNumber());
        list.add(" Wersja: " + computerSystem.getBaseboard().getVersion());
        list.add("");
        list.add("BIOS: " + computerSystem.getFirmware().getName());
        list.add(" Dystrybucja: " + computerSystem.getFirmware().getManufacturer());
        list.add(" Wersja: " + computerSystem.getFirmware().getVersion());
        list.add(" Data wydania: " + computerSystem.getFirmware().getReleaseDate());
        ObservableList<String> observableArrayList =
                FXCollections.observableArrayList(list);
        listView.setItems(observableArrayList);
    }
    public void printSystem(final OperatingSystem os) {
        list.add(String.valueOf(os));
        list.add("Booted: " + Instant.ofEpochSecond(os.getSystemBootTime()));
        list.add("Uptime: " + FormatUtil.formatElapsedSecs(os.getSystemUptime()));
        list.add("Running with" + (os.isElevated() ? "" : "out") + " elevated permissions.");
        ObservableList<String> observableArrayList =
                FXCollections.observableArrayList(list);
        listView.setItems(observableArrayList);
    }
    public void printProcesses(OperatingSystem os, GlobalMemory memory) {
        list.add("Processes: " + os.getProcessCount() + ", Threads: " + os.getThreadCount());
        // Sort by highest CPU
        List<OSProcess> procs = Arrays.asList(os.getProcesses(5, OperatingSystem.ProcessSort.CPU));

        list.add("   PID  %CPU %MEM       VSZ       RSS Name");
        for (int i = 0; i < procs.size() && i < 5; i++) {
            OSProcess p = procs.get(i);
            list.add(String.format(" %5d %5.1f %4.1f %9s %9s %s", p.getProcessID(),
                    100d * (p.getKernelTime() + p.getUserTime()) / p.getUpTime(),
                    100d * p.getResidentSetSize() / memory.getTotal(), FormatUtil.formatBytes(p.getVirtualSize()),
                    FormatUtil.formatBytes(p.getResidentSetSize()), p.getName()));
        }

        ObservableList<String> observableArrayList =
                FXCollections.observableArrayList(list);
        listView.setItems(observableArrayList);
    }

    public void printUsbDevices(UsbDevice[] usbDevices) {
        for (UsbDevice usbDevice : usbDevices) {
            list.add(String.valueOf(usbDevice));
        }
        ObservableList<String> observableArrayList =
                FXCollections.observableArrayList(list);
        listView.setItems(observableArrayList);
    }

    public void printDisplays(Display[] displays) {
        int i = 1;
        for (Display display : displays) {
            list.add(" Monitor " + i + ":");
            list.add(String.valueOf(display));
            i++;
        }
        ObservableList<String> observableArrayList =
                FXCollections.observableArrayList(list);
        listView.setItems(observableArrayList);
    }

    public void printSoundCards(SoundCard[] cards) {
        for (SoundCard card : cards) {
            list.add(" " + String.valueOf(card));
        }

        ObservableList<String> observableArrayList =
                FXCollections.observableArrayList(list);
        listView.setItems(observableArrayList);
    }

    public void printFileSystem(FileSystem fileSystem) {
        list.add(String.format(" File Descriptors: %d/%d", fileSystem.getOpenFileDescriptors(),
                fileSystem.getMaxFileDescriptors()));

        OSFileStore[] fsArray = fileSystem.getFileStores();
        for (OSFileStore fs : fsArray) {
            long usable = fs.getUsableSpace();
            long total = fs.getTotalSpace();
            list.add(String.format(
                    " %s (%s) [%s] %s of %s free (%.1f%%), %s of %s files free (%.1f%%) is %s "
                            + (fs.getLogicalVolume() != null && fs.getLogicalVolume().length() > 0 ? "[%s]" : "%s")
                            + " and is mounted at %s",
                    fs.getName(), fs.getDescription().isEmpty() ? "file system" : fs.getDescription(), fs.getType(),
                    FormatUtil.formatBytes(usable), FormatUtil.formatBytes(fs.getTotalSpace()), 100d * usable / total,
                    FormatUtil.formatValue(fs.getFreeInodes(), ""), FormatUtil.formatValue(fs.getTotalInodes(), ""),
                    100d * fs.getFreeInodes() / fs.getTotalInodes(), fs.getVolume(), fs.getLogicalVolume(),
                    fs.getMount()));
        }

        ObservableList<String> observableArrayList =
                FXCollections.observableArrayList(list);
        listView.setItems(observableArrayList);
    }

    private void printPowerSources(PowerSource[] powerSources) {
        StringBuilder sb = new StringBuilder("Power Sources: ");
        if (powerSources.length == 0) {
            list.add("Nie znaleziono.");
        }
        for (PowerSource powerSource : powerSources) {
            list.add(powerSource.getName());
            list.add("Pozostało " + powerSource.getRemainingCapacity()*100 + "%");
            list.add("Pozostały czas pracy: " + powerSource.getTimeRemaining()/60 + "min");
        }

        ObservableList<String> observableArrayList =
                FXCollections.observableArrayList(list);
        listView.setItems(observableArrayList);
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
