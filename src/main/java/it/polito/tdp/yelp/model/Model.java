 package it.polito.tdp.yelp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.yelp.db.YelpDao;

public class Model {
	
	private Graph<User, DefaultWeightedEdge> grafo;
	private YelpDao dao;
	private List<User> utenti;
	private Map<String, User> idMap;
	
	public String creaGrafo(int minRevisioni, int anno) {
		dao = new YelpDao();
		idMap = new HashMap<>();
		
		for(User user : dao.getAllUsers()) {
			idMap.put(user.getUserId(), user);
		}
		
		grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		utenti = dao.getVertici(idMap, minRevisioni);
		//utenti = dao.getUsersWithReviews(minRevisioni); // Sono i vertici del grafo
		Graphs.addAllVertices(grafo, utenti);
		
		for(User u1 : utenti) {
			for(User u2 : utenti) {
				// u1.getUserId().compareTo(u2.getUserId()) < 0 --> evito di creare lo stesso arco due volte (ottimizzazione)
				if(!u1.equals(u2) && u1.getUserId().compareTo(u2.getUserId()) < 0) { // Se gli utenti sono diversi conto la similarità tra gli utenti 
																					 // e se è > 0 aggiungo l'arco
					int sim = dao.calcolaSimilarita(u1, u2, anno);
					if(sim > 0) {
						Graphs.addEdge(grafo, u1, u2, sim);
					}
				}
			}
		}
		
		return "Grafo creato con " + grafo.vertexSet().size() + " vertici e " + grafo.edgeSet().size() + " archi";
	}
	
	public List<User> getUser() {
		return utenti;
	}
	
	public List<User> utentiPiuSimili(User u) {
		// Dobbiamo calcolare il peso massimo sugli archi adiacenti e trovare poi tutti gli archi che coincidono con questo massimo
		int max = 0;
		
		// grafo.edgesOf(u) --> mi da gli archi incidenti sul vertice u
		for(DefaultWeightedEdge edge : grafo.edgesOf(u)) {
			if(grafo.getEdgeWeight(edge) > max) {
				max = (int)grafo.getEdgeWeight(edge);
			}
		}
		
		List<User> result = new ArrayList<>();
		// Controllo solo gli archi il cui peso è uguale a max
		for(DefaultWeightedEdge edge : grafo.edgesOf(u)) {
			if(grafo.getEdgeWeight(edge) == max) {
				// Prendo il vertice dell'arco che non è u stesso
				User u2 = Graphs.getOppositeVertex(grafo, edge, u);
				result.add(u2);
			}
		}
		
		return result;
	}
}
