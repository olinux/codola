package ch.olischmid.codola.process.data;

import lombok.Data;

@Data
public class DocumentOrder {

	private FinalFormat finalFormat;
	private DeliveryType deliveryType;
}
