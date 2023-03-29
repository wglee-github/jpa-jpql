package jpql;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;

@Entity
//@NamedQuery(
//		name = "Member.findByUsername",
//		query = "select m from Member m where m.name = :name")
public class Member {

	@Id @GeneratedValue
	private Long id;
	
	private String name;
	
	private String comp;
	
	private int age;

	/**
	 * @ManyToOne 의 fetch default는 EAGER 이다.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TEAM_ID")
	private Team team;
	
	@Enumerated(EnumType.STRING)
	private MemberType type;
	
	public Member() {
		super();
	}

	public Member(String name, int age) {
		super();
		this.name = name;
		this.age = age;
	}

	/**
	 * 
	 * @param team
	 * 
	 * 양방향 연관관계 편의 메소드
	 */
	public void changeTeam(Team team) {
		this.team = team;
		team.getMembers().add(this);
	}
	
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

	
	public String getComp() {
		return comp;
	}

	public void setComp(String comp) {
		this.comp = comp;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Team getTeam() {
		return team;
	}

	private void setTeam(Team team) {
		this.team = team;
	}

	public MemberType getType() {
		return type;
	}

	public void setType(MemberType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Member [id=" + id + ", name=" + name + ", age=" + age + "]";
	}
	
}
