package test;

import java.util.HashSet;
import java.util.Set;

public class Test {
	
	public static void main(String[] args) {
		
		String source = "RunwayVisibleRangeMeasurement";
		String target = "monograph";

		Set<String> subclasses = new HashSet<String>();
		subclasses.add("RunwayVisibleRange");
		subclasses.add("RunwayVisible");
		subclasses.add("Object");
		
		System.out.println(compoundRatioMatch(source, subclasses));

		
		
	}
	
	public static boolean compoundRatioMatch(String source, Set<String> target) {
		

		int max = 0;
		boolean match = false;
		
		String[] sourceArray = source.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
		String[] targetArray = null;
		
		
		for (String t : target) {
						
			targetArray = t.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
			
			max = Math.max(sourceArray.length, targetArray.length);
			int equal = 0;
			
			for (int j = 0; j < targetArray.length; j++) {
				
				double ratio = 0;
				
				for (int i = 0; i < sourceArray.length; i++) {
					
					if (sourceArray[i].equalsIgnoreCase(targetArray[j])) {

						equal++;
					}
					

					
				}
				ratio = (double) equal / (double) max;
				System.out.println("ratio is " + ratio);
				if (ratio >= 0.75) {
					match = true;
				}

			}

		}
		
		return match;
		
	}
		
		
//		for (int i = 0; i < sourceArray.length; i++) {
//			
//			for (String t : target) {
//
//				targetArray = t.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
//				
//				max = Math.max(sourceArray.length, targetArray.length);
//				
//				for (int j = 0; j < targetArray.length; j++) {
//
//					if (sourceArray[i].equalsIgnoreCase(targetArray[j])) {
//						System.out.println("sourceArray[i] is " + sourceArray[i]);
//						System.out.println("targetArray[j] is " + targetArray[j]);
//						equal++;
//					}
//				
//				}
//				
//				
//				
//			}
//				
//		}
//		
//		ratio = (double) equal / (double) max;
//		System.out.println("equal is " + equal + ", " + "max is " + max);
//		System.out.println("Ratio is " + ratio);
//		
//		if (ratio >= 0.75) {
//			return true;
//		} else {
//			return false;
//		}
		
		
//	}
	
	public static boolean synonymMatch(Set<String> synonyms, Set<String> concepts) {
		
		boolean match = false;
		
		for (String syn : synonyms) {
			if (concepts.contains(syn)) {
				match = true;
			}
		}
		
		return match;
		
	}

}
