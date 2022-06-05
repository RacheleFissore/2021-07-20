package it.polito.tdp.yelp.model;

public class Event implements Comparable<Event> {
	public enum EventType {
		DA_INTERVISTARE,
		FERIE
	}
	
	// Attributi dell'evento
	private int giorno; // E' il nostro tempo
	private EventType type;
	private User intervistato;
	private Giornalista giornalista; // E' colui che dovrà intervistare l'utente
	
	public Event(int giorno, EventType type, User intervistato, Giornalista giornalista) {
		super();
		this.giorno = giorno;
		this.type = type;
		this.intervistato = intervistato;
		this.giornalista = giornalista;
	}
	public int getGiorno() {
		return giorno;
	}
	public void setGiorno(int giorno) {
		this.giorno = giorno;
	}
	
	public EventType getType() {
		return type;
	}
	
	public User getIntervistato() {
		return intervistato;
	}
	public void setIntervistato(User intervistato) {
		this.intervistato = intervistato;
	}
	public Giornalista getGiornalista() {
		return giornalista;
	}
	public void setGiornalista(Giornalista giornalista) {
		this.giornalista = giornalista;
	}
	@Override
	public int compareTo(Event o) {
		return this.giorno - o.giorno;
	}
	
	
}
