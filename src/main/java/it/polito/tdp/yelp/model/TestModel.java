package it.polito.tdp.yelp.model;

public class TestModel {

	public static void main(String[] args) {
		Model model = new Model();
		String msg = model.creaGrafo(200, 2007);
		System.out.print(msg);
	}

}
