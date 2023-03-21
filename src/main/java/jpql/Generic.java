package jpql;

import java.util.ArrayList;
import java.util.List;

public class Generic<G> {

	List<G> list = new ArrayList<G>();
	
	
	public void add(G o) {
		this.list.add(o);
	}
	
	public List<G> getList() {
		
		return this.list;
	}
}
