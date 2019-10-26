package sample;

import com.sun.jna.platform.unix.X11;
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
import oshi.util.ExecutingCommand;
import oshi.util.FormatUtil;
import oshi.util.Util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

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
    public Button graphicCardBtn;
    @FXML
    public ListView<String> listView;


    public void getCpu() {
        listView.setItems(null);
        list.clear();
        category.setText("Processor");
        printProcessor(hal.getProcessor());
    }

    public void getMobo() {
        listView.setItems(null);
        list.clear();
        category.setText("Motherboard");
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
        category.setText("Sensors");
        printSensor(hal.getSensors());
    }

    public void getDisk() {
        listView.setItems(null);
        list.clear();
        category.setText("Disks");
        printDisk(hal.getDiskStores());
    }

    public void getProcesses() {
        listView.setItems(null);
        list.clear();
        category.setText("Processes");
        printProcesses(os, hal.getMemory());
    }

    public void getUsbDevices() {
        listView.setItems(null);
        list.clear();
        category.setText("USB Devices");
        printUsbDevices(hal.getUsbDevices(true));
    }

    public void getDisplays() {
        listView.setItems(null);
        list.clear();
        category.setText("Displays");
        printDisplays(hal.getDisplays());
    }

    public void getSoundCards() {
        listView.setItems(null);
        list.clear();
        category.setText("Sound cards");
        printSoundCards(hal.getSoundCards());
    }

    public void getFileSystem() {
        listView.setItems(null);
        list.clear();
        category.setText("File system");
        printFileSystem(os.getFileSystem());
    }

    public void getPowerSources() {
        listView.setItems(null);
        list.clear();
        category.setText("Power sources");
        printPowerSources(hal.getPowerSources());
    }

    public void getGraphicCards() {
        listView.setItems(null);
        list.clear();
        category.setText("Graphic cards");
        printGraphicCards();
    }

    public void printProcessor(CentralProcessor processor) {
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
        // per core CPU
        list.add("CPU load per thread:");
        double[] load = processor.getProcessorCpuLoadBetweenTicks(prevProcTicks);
        for (int i = 0; i < load.length; i++) {
            double avg = load[i];
            list.add("  Thread " + (i + 1) + ": " + String.format(" %.1f%%", avg * 100));
        }
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
            list.add("Current Frequencies: ");
            for (int i = 0; i < freqs.length; i++) {
                list.add("  Core " + (i + 1) + ": " + FormatUtil.formatHertz(freqs[i]));
            }
            ObservableList<String> observableArrayList =
                    FXCollections.observableArrayList(list);
            listView.setItems(observableArrayList);
        }
    }

    private void printDisk(HWDiskStore[] diskStores) {
        for (HWDiskStore disk : diskStores) {
            list.add(" Name: " + disk.getName());
            list.add(" Model: " + disk.getModel());
            list.add(" Serial number: " + disk.getSerial());
            list.add(" Disk size: " + (disk.getSize()/1073741824) + "GB");
            list.add(" Transfer time: " + (disk.getTransferTime()/60000) + "min");
            Format f = new SimpleDateFormat("dd-MM-yyyy");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
            Date date = null;
            try {
                date = df.parse(Instant.now().minusMillis(disk.getTimeStamp()).toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String timestamp = f.format(date);
            list.add(" Timestamp: " + timestamp);

            list.add(" Partitions: ");
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
        list.add("Motherboard: " + computerSystem.getBaseboard().getModel());
        list.add(" Manufacturer: " + computerSystem.getBaseboard().getManufacturer());
        list.add(" Serial number: " + computerSystem.getBaseboard().getSerialNumber());
        list.add(" Version: " + computerSystem.getBaseboard().getVersion());
        list.add("");
        list.add("Firmware: " + computerSystem.getFirmware().getName());
        list.add(" Manufacturer: " + computerSystem.getFirmware().getManufacturer());
        list.add(" Version: " + computerSystem.getFirmware().getVersion());
        list.add(" Release date: " + computerSystem.getFirmware().getReleaseDate());
        ObservableList<String> observableArrayList =
                FXCollections.observableArrayList(list);
        listView.setItems(observableArrayList);
    }
    private void printSystem(final OperatingSystem os) {
        list.add(String.valueOf(os));

        Format f = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date date = null;
        try {
            date = df.parse(Instant.ofEpochSecond(os.getSystemBootTime()).toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String updateDate = f.format(date);
        list.add("Last update: " + updateDate);
        list.add("Uptime: " + FormatUtil.formatElapsedSecs(os.getSystemUptime()));
        list.add("Serial number: " + ExecutingCommand.getAnswerAt("wmic path softwarelicensingservice get OA3xOriginalProductKey", 2));
        list.add("Running with" + (os.isElevated() ? "" : "out") + " elevated permissions.");
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
            list.add(" Display " + i + ":");
            list.add(String.valueOf(display));
            list.add("");
            i++;
        }
        ObservableList<String> observableArrayList =
                FXCollections.observableArrayList(list);
        listView.setItems(observableArrayList);
    }

    public void printSoundCards(SoundCard[] cards) {
        for (SoundCard card : cards) {
            list.add(" Name: " + card.getName());
            list.add(" Codec: " + card.getCodec());
            list.add(" Version: " + card.getDriverVersion());
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
            list.add("Not found.");
        }
        for (PowerSource powerSource : powerSources) {
            list.add("Nazwa źródła: " + powerSource.getName());
            String f = String.format("%.2f", powerSource.getRemainingCapacity()*100);
            list.add("Remaining: " + f + "%");
            if (powerSource.getTimeRemaining()<0) {
                list.add("Time left: charging");
            } else {
                f = String.format("%.2f", (powerSource.getTimeRemaining()/60));
                list.add("Time left: " + f + "min");
            }
        }

        ObservableList<String> observableArrayList =
                FXCollections.observableArrayList(list);
        listView.setItems(observableArrayList);
    }

    private void printGraphicCards() {
        try {
            String filePath = "./dxdiag_output.txt";
            ProcessBuilder pb = new ProcessBuilder("cmd.exe","/c","dxdiag","/t",filePath);
            Process p = pb.start();
            p.waitFor();
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            String line;
            int i=0;
            while((line = br.readLine()) != null){
                if(line.trim().startsWith("Card name:")
                        || line.trim().startsWith("Current Mode:")
                        || line.trim().startsWith("Dedicated Memory:")){
                    list.add(line.trim());
                    i++;
                    if (i==3) {
                        list.add("");
                        i=0;
                    }
                }
            }
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
        if ((list.isEmpty())) {
            list.add("Graphic cards not found.");
        }
        ObservableList<String> observableArrayList =
                FXCollections.observableArrayList(list);
        listView.setItems(observableArrayList);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
