package SemFormToSWRL;


public class Atom {
	String atomName;
	String atomArg1;
	String atomArg2;
	String atomType;
	
	//for classes
	public Atom(String atomName, String atomArg1, String atomType) {
		this.atomName = atomName; 
		this.atomArg1 = atomArg1;
		this.atomType = atomType;
	}
	
	//for properties
	public Atom(String atomName, String atomArg1, String atomArg2, String atomType) {
		this.atomName = atomName; 
		this.atomArg1 = atomArg1;
		this.atomArg2 = atomArg2;
		this.atomType = atomType;
	}

	public Atom() {
	}
	
}
