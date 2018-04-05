package ee.ttu.idu0080.raamatupood.types;

import java.io.Serializable;

public class TellimusRida implements Serializable  {
	private static final long serialVersionUID = 1L;
	public Toode toode;
	public Long kogus;
	
	public TellimusRida(Toode toode, Long kogus)
	{
		this.toode = toode;
		this.kogus = kogus;
	}
	
	public Long getKogus() {
		return this.kogus;
	}
	
	public Toode getToode() {
		return this.toode;
	}

	@Override
	public String toString() {
		return "TellimusRida [toode=" + toode + ", kogus=" + kogus + "]";
	}
}
