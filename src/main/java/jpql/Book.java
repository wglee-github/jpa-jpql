package jpql;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("BB")
public class Book extends Item {
	
	private String store;

	public String getStore() {
		return store;
	}

	public void setStore(String store) {
		this.store = store;
	}

	
}
