package test;

public class TestRegEx {
	
	public static void main(String[] args) {
		
		String uri = "<http://www.test.no>";
		//s.replaceAll("[\\.$|,|;|']", "")
		String regex = uri.replaceAll("[<|>]", "");
		
		System.out.println("The fixed text is now " + regex);
		
	}

}
