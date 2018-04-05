package ee.ttu.idu0080.raamatupood.types;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Tellimus implements Serializable {
	private static final long serialVersionUID = 1L;
	private List<TellimusRida> tellimuseRead;
	
	public Tellimus() {
		tellimuseRead = new ArrayList<TellimusRida>();
	}
	
	public void addTellimusRida(TellimusRida tellimusRida) {
		this.tellimuseRead.add(tellimusRida);
	}
	
	public List<TellimusRida> getTellimusRead() {
		return tellimuseRead;
	}

	@Override
	public String toString() {
		return "Tellimus [tellimuseRead=" + tellimuseRead + "]";
	}
}
