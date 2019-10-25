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
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private SystemInfo si = new SystemInfo();
    private HardwareAbstractionLayer hal = si.getHardware();
    private OperatingSystem os = si.getOperatingSystem();
    private List<String> list = new ArrayList<>();

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
        listView.setItems(null);
        list.clear();
        category.setText("Źródła zasilania");
        printPowerSources(hal.getPowerSources());
    }

    private void printProcessor(CentralProcessor processor) {
        list.add(processor.toString());
        list.add("Przełączenia kontekstowe / przerwania: " + processor.getContextSwitches() + " / " + processor.getInterrupts());

        long[] prevTicks = processor.getSystemCpuLoadTicks();
        long[][] prevProcTicks = processor.getProcessorCpuLoadTicks();
        list.add("CPU, IOWait, i IRQ ticks @ 0 sec:" + Arrays.toString(prevTicks));
        Util.sleep(1000);
        long[] ticks = processor.getSystemCpuLoadTicks();
        list.add("CPU, IOWait, and IRQ @ 1 sec:" + Arrays.toString(ticks));
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
        list.add(String.format("Obciążenie procesora: %.1f%%", processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100));
        double[] loadAverage = processor.getSystemLoadAverage(3);
        list.add("Średnie obciążenie procesora:" + (loadAverage[0] < 0 ? " N/A" : String.format(" %.2f", loadAverage[0]))
                + (loadAverage[1] < 0 ? " N/A" : String.format(" %.2f", loadAverage[1]))
                + (loadAverage[2] < 0 ? " N/A" : String.format(" %.2f", loadAverage[2])));
        // per core CPU
        StringBuilder procCpu = new StringBuilder("Obciążenie procesora na rdzeń:");
        double[] load = processor.getProcessorCpuLoadBetweenTicks(prevProcTicks);
        for (double avg : load) {
            procCpu.append(String.format(" %.1f%%", avg * 100));
        }
        list.add(procCpu.toString());
        long freq = processor.getVendorFreq();
        if (freq > 0) {
            list.add("Częstotliwość taktowania zegara: " + FormatUtil.formatHertz(freq));
        }
        freq = processor.getMaxFreq();
        if (freq > 0) {
            list.add("Maksymalna częstotliwość taktowania zegara: " + FormatUtil.formatHertz(freq));
        }
        long[] freqs = processor.getCurrentFreq();
        if (freqs[0] > 0) {
            StringBuilder sb = new StringBuilder("Aktualne częstotliwości: ");
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

    private void printDisk(HWDiskStore[] diskStores) {
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

    private void printSensor(Sensors sensors) {
        if (sensors.getCpuTemperature() != 0) {
            list.add("Temperatura dysku: " + sensors.getCpuTemperature());
        }
        if (sensors.getCpuVoltage() != 0) {
            list.add("Napięcie dysku: " + sensors.getCpuVoltage());
        }
        if (sensors.getFanSpeeds().length > 0) {
            int[] speeds = sensors.getFanSpeeds();
            for (int i = 0; i < speeds.length; i++) {
                list.add("Prędkość wentylatora: " + i + " - " + speeds[i] + "obr/min");
            }
            ObservableList<String> observableArrayList =
                    FXCollections.observableArrayList(list);
            listView.setItems(observableArrayList);
        }
    }
    private void printMobo(ComputerSystem computerSystem) {
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
    private void printSystem(final OperatingSystem os) {
        list.add(String.valueOf(os));
        list.add("Data uruchomienia: " + Instant.ofEpochSecond(os.getSystemBootTime()));
        list.add("Czas pracy: " + FormatUtil.formatElapsedSecs(os.getSystemUptime()));
        list.add("Uruchomiony " + (os.isElevated() ? "z podwyższonymi uprawnieniami." : "bez podwyższonych uprawnieniień."));
        ObservableList<String> observableArrayList =
                FXCollections.observableArrayList(list);
        listView.setItems(observableArrayList);
    }
    private void printProcesses(OperatingSystem os, GlobalMemory memory) {
        list.add("Ilość procesów: " + os.getProcessCount() + ", Ilość wątków: " + os.getThreadCount());
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

    private void printUsbDevices(UsbDevice[] usbDevices) {
        for (UsbDevice usbDevice : usbDevices) {
            list.add(String.valueOf(usbDevice));
        }
        ObservableList<String> observableArrayList =
                FXCollections.observableArrayList(list);
        listView.setItems(observableArrayList);
    }

    private void printDisplays(Display[] displays) {
        int i = 1;
        for (Display display : displays) {
            list.add(" Monitor " + i + ":");
            list.add(String.valueOf(display));
            list.add("");
            i++;
        }
        ObservableList<String> observableArrayList =
                FXCollections.observableArrayList(list);
        listView.setItems(observableArrayList);
    }

    private void printSoundCards(SoundCard[] cards) {
        for (int i = 0; i < cards.length; i++) {
            int index = i + 1;
            list.add(index + ". " + cards[i].getName() + ": ");
            list.add("  Kodek: " + cards[i].getCodec());
            list.add("  Wersja sterownika: " + cards[i].getDriverVersion());
            list.add("");
        }

        ObservableList<String> observableArrayList =
                FXCollections.observableArrayList(list);
        listView.setItems(observableArrayList);
    }

    private void printFileSystem(FileSystem fileSystem) {
        list.add(String.format("Deskryptory plików: %d/%d", fileSystem.getOpenFileDescriptors(),
                fileSystem.getMaxFileDescriptors()));
        DecimalFormat df = new DecimalFormat("#.##");

        OSFileStore[] fsArray = fileSystem.getFileStores();
        for (int i = 0; i < fsArray.length; i++) {
            OSFileStore fs = fsArray[i];
            long usable = fs.getUsableSpace();
            long total = fs.getTotalSpace();
            int index = i + 1;
            list.add("Dysk #" + index + ": ");
            list.add("  Nazwa: " + fs.getName());
            list.add("  Wolumin: " + fs.getLogicalVolume());
            list.add("  Wolumin logiczny: " + fs.getVolume());
            list.add("  Opis: " + (fs.getDescription().isEmpty() ? "system plików" : fs.getDescription()));
            list.add("  Typ: " + fs.getType());
            double fsp = 100d * usable / total;
            double fip = 100d * fs.getFreeInodes() / fs.getTotalInodes();
            list.add("  Wolne miejsce: " + FormatUtil.formatBytes(usable) + (fsp > 0 ?  " (" + df.format(fsp) + "%)" : "") + " z " + FormatUtil.formatBytes(fs.getTotalSpace()));
            list.add("  Wolne i-węzły: " + FormatUtil.formatValue(fs.getFreeInodes(), "") + (fip > 0 ?  " (" + df.format(fip) + "%)" : "") + " z " + FormatUtil.formatValue(fs.getTotalInodes(), ""));
        }

        ObservableList<String> observableArrayList =
                FXCollections.observableArrayList(list);
        listView.setItems(observableArrayList);
    }

    private void printPowerSources(PowerSource[] powerSources) {
        if (powerSources.length == 0) {
            list.add("Nie znaleziono.");
        }
        for (PowerSource powerSource : powerSources) {
            list.add("Nazwa źródła: " + powerSource.getName());
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
