package it.polito.tdp.crimes.model;

import java.util.LinkedList;
import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.crimes.db.EventsDao;

public class Model {
	
	private SimpleWeightedGraph<String, DefaultWeightedEdge> grafo; 
	private EventsDao dao; 
	private List <String> percorsoMigliore;
	
	public Model() {
		dao = new EventsDao();
	}
	
	public void creaGrafo(String categoria, int mese) {
		this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		//Aggiunta dei vertici
		Graphs.addAllVertices(grafo, dao.getVertici(categoria, mese));
		
		//aggiunta degli archi
		for(Adiacenze a: dao.getAdiacenze(categoria, mese)) {
			if(this.grafo.getEdge(a.getV1(), a.getV2())==null) {
				Graphs.addEdgeWithVertices(grafo, a.getV1(), a.getV2(), a.getPeso());
			}
		}
		System.out.println("#Vertici: "+this.grafo.vertexSet().size());
		System.out.println("#Archi: "+this.grafo.edgeSet().size());
	}
	
	public List <Adiacenze> getArchi(){
		//calcolo il peso medio degli archi presenti nel grafo
		double pesoMedio = 0.0; 
		for(DefaultWeightedEdge e: this.grafo.edgeSet()) {
			pesoMedio += this.grafo.getEdgeWeight(e);
		}
		pesoMedio = pesoMedio/this.grafo.edgeSet().size(); 
		//filtro gli archi, tenendo solo quelli che hanno peso maggiore di quello medio
		List <Adiacenze> result =new LinkedList<>();
		for(DefaultWeightedEdge e: this.grafo.edgeSet()) {
			if(this.grafo.getEdgeWeight(e)>pesoMedio)
				result.add(new Adiacenze(this.grafo.getEdgeSource(e), this.grafo.getEdgeTarget(e), this.grafo.getEdgeWeight(e)));
			
		}
		return result; 
	}

	public List <String> trovaPercorso(String sorgente, String destinazione){
		this.percorsoMigliore = new LinkedList<>(); 
		List <String> parziale = new LinkedList<>();
		parziale.add(sorgente);
		cerca(destinazione, parziale, 0);
		return this.percorsoMigliore;
	}
	
	private void cerca(String destinazione, List <String> parziale, int L) {
		//caso terminale
		if(parziale.get(parziale.size()-1).equals(destinazione)) {
			if(parziale.size()>this.percorsoMigliore.size()) {
				this.percorsoMigliore = new LinkedList<>(parziale);
			}
			return; 
		}
		//altrimenti scorro i vicini dell'ultimo inserito e provo ad aggiungerli uno ad uno
		for(String vicino: Graphs.neighborListOf(grafo, parziale.get(parziale.size()-1))) {
			if(!parziale.contains(vicino)) {
				parziale.add(vicino);
				cerca(destinazione, parziale, L+1);
				parziale.remove(parziale.size()-1);
			}
		}
	}
}

