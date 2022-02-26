import java.io.Serializable;

public class Gift implements Serializable {
    int serial;
    int factory_code;
    int elf_serial;
    public Gift(int factory_code, int elf_serial, int serial) {
        this.factory_code = factory_code;
        this.elf_serial = elf_serial;
        this.serial = serial;
    }
}
