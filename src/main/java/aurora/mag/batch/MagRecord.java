package aurora.mag.batch;

import lombok.Data;

@Data
public class MagRecord {
    private String timestamp;
    private Float Xcomponent;
    private Float Ycomponent;
    private Float Zcomponent;
    private Float deriviativeX;
}
