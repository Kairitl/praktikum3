package ee.ttu.idu0080.raamatupood.types;

import java.io.Serializable;
import java.math.BigDecimal;

public class Toode implements Serializable  {
	private static final long serialVersionUID = 1L;
	public int kood;
	public String nimetus;
	public BigDecimal hind;
	
	public Toode(int kood, String nimetus, String hind) {
		this.kood = kood;
		this.nimetus = nimetus;
		this.hind = new BigDecimal(hind);
	}

	@Override
	public String toString() {
		return "Toode [kood=" + kood + ", nimetus=" + nimetus + ", hind=" + hind + "]";
	}
}
