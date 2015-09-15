package microsec.freddysbbq.menu.model.v1;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class MenuItem {

    private long id;
    private String name;
    private BigDecimal price;

}