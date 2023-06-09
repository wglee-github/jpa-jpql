package jpql;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Team {

	@Id @GeneratedValue
	private Long id;
	
	private String name;

	/**
	 * global 옵션은 persistence.xml에 선언하면 된다.
	 * <property name="hibernate.default_batch_fetch_size" value="100"/>
	 */
//	@BatchSize(size = 100)
	@OneToMany(mappedBy = "team")
	private List<Member> members = new ArrayList<Member>();
	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Member> getMembers() {
		return members;
	}

	public void setMembers(List<Member> members) {
		this.members = members;
	}

	@Override
	public String toString() {
		return "Team [id=" + id + ", name=" + name + "]";
	}
}
