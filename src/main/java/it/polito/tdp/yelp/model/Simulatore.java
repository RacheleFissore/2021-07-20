package it.polito.tdp.yelp.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;

import it.polito.tdp.yelp.model.Event.EventType;

public class Simulatore {
	// Dati in ingresso
	private int x1; // Numero di giornalisti
	private int x2; // Numero di utenti da intervistare
	
	// Dati in uscita (da calcolare)
	
	// I giornalisti sono rappresentati da un numero compreso tra 0 e x1-1, ognuno di questi deve ricordarsi quante persone ha intervistato
	private List<Giornalista> giornalisti;
	private int numeroGiorni;
	
	// Modello del mondo
	
	// Devo sapere chi sono le persone già intervistate e chi non lo è ancora, quando il numero delle persone intervistate raggiunge x2 mi fermo
	// Le persone intervistate sono tolte dall'elenco
	private Set<User> intervistati; // Quando aggiungo un utente intervistato qui devo fare in modo che questo utente non sia più intervistato in futuro
	private Graph<User, DefaultWeightedEdge> grafo;
	
	// Coda degli eventi
	private PriorityQueue<Event> queue;
	
	public Simulatore(Graph<User, DefaultWeightedEdge> grafo) {
		this.grafo = grafo;
	}
	
	public void init(int x1, int x2) {
		this.x1 = x1;
		this.x2 = x2;
		
		intervistati = new HashSet<User>();
		numeroGiorni = 0; 
		giornalisti = new ArrayList<>();
		for(int id = 0; id < x1; id++) {
			giornalisti.add(new Giornalista(id)); // Ciasun gionalista all'inizio ha 0 intervistati
		}
		
		// Pre-carico la coda
		for(Giornalista g : giornalisti) {
			// L'elenco dei possibili user da intervistare sono i vertici del grafo
			User intervistato = selezionaIntervistato(this.grafo.vertexSet()); 
			
			intervistati.add(intervistato);
			g.incrementaNumeroIntervistati();
			
			// 1 è il giorno in cui siamo
			queue.add(new Event(1, EventType.DA_INTERVISTARE, intervistato, g));
		}
	}
	
	public void run() {
		// Devo avere intervistato x2 persone prima di fermarmi
		while (!queue.isEmpty() && intervistati.size() < x2) {
			Event e = queue.poll();
			numeroGiorni = e.getGiorno();
			
			processEvent(e);
		}
	}

	private void processEvent(Event e) {
		switch (e.getType()) {
			case DA_INTERVISTARE:
				double caso = Math.random();
				
				if(caso < 0.6) {
					// Caso 1
					// Devo vedere se l'evento attuale ha dei vicini non ancora intervistati, se li ha deve scegliere il massimo come peso, se ce n'è più di uno
					// dovrà sceglierne uno casuale tra questi
					
					User vicino = selezionaAdiacente(e.getIntervistato());
					if(vicino == null) {
						// Se non ho vicini l'intervistato deve essere scelto dal grafo
						vicino = selezionaIntervistato(grafo.vertexSet());
					}
					
					queue.add(new Event(e.getGiorno()+1, EventType.DA_INTERVISTARE, vicino, e.getGiornalista()));
					intervistati.add(vicino);
					e.getGiornalista().incrementaNumeroIntervistati(); // Aggiunge un intervistato al giornalista
				}
				else if(caso < 0.8) {
					// Caso 2: non scelgo adesso chi dovrò intervistare tra due giorni, mi metto in ferie il giorno dopo
					queue.add(new Event(e.getGiorno()+1, EventType.FERIE, e.getIntervistato(), e.getGiornalista()));
				}
				else {
					// Caso 3: domani continuo con lo stesso utente
					// Aggiungo alla coda un evento uguale a quello a cui sto processando tranne per il giorno, che a questo punto sarà il giorno dopo
					queue.add(new Event(e.getGiorno()+1, EventType.DA_INTERVISTARE, e.getIntervistato(), e.getGiornalista()));
				}
				break;
	
			case FERIE:
				break;
				
			default:
				break;
		}
		
	}

	public int getX1() {
		return x1;
	}

	public void setX1(int x1) {
		this.x1 = x1;
	}

	public int getX2() {
		return x2;
	}

	public void setX2(int x2) {
		this.x2 = x2;
	}

	public List<Giornalista> getGiornalisti() {
		return giornalisti;
	}

	public int getNumeroGiorni() {
		return numeroGiorni;
	}
	
	/**
	 * Seleziona un intervistato dalla lista specificata evitando di selezionare coloro che sono già presenti in this.intervistati
	 * @param lista
	 * @return
	 */
	private User selezionaIntervistato(Collection<User> lista) { // Lista: sono i vertici del grafo all'inizio
		Set<User> candidati = new HashSet<User>(lista);
		candidati.removeAll(intervistati); // Dai possibili candidati tolgo coloro che sono già stati intervistati
		
		int scelto = (int)(Math.random()*candidati.size());
		return (new ArrayList<User>(candidati)).get(scelto);
	}
	
	private User selezionaAdiacente(User u) {
		// Mi prendo i vicini di un certo utente, tolgo quelli già intervistati e vedo se ne resta qualcuno
		List<User> vicini = Graphs.neighborListOf(grafo, u);
		vicini.removeAll(intervistati); // Dalla lista dei vicini tolgo quelli che sono già stati intervistati
		
		if(vicini.size() == 0)
			return null; // Capita quando il vertice è isolato oppure tutti gli adiacenti sono già stati intervistati
		
		// Calcolo il massimo ora
		double max = 0;
		for(User v : vicini) {
			double peso = grafo.getEdgeWeight(grafo.getEdge(u, v)); // Prendo il peso dell'arco che collega u e v
			if(peso > max) {
				max = peso;
			}
		}
		
		// Creo una nuova lista di migliori che hanno peso = max
		List<User> migliori = new ArrayList<>();
		for(User v : vicini) {
			double peso = grafo.getEdgeWeight(grafo.getEdge(u, v)); // Prendo il peso dell'arco che collega u e v
			if(peso == max) {
				migliori.add(v);
			}			
		}
		
		int scelto = (int)(Math.random()*migliori.size()); // Se size() = 1 la math.random() mi restituisce un numero tra 0 e 1, approssimato a (int) mi restituisce 0 che
														   // sarà l'unico elemento della lista
		return migliori.get(scelto);
	}
}

