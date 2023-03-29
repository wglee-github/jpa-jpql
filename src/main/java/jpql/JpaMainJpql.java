package jpql;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.FetchType;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

/**
 * 
 * @author wglee
 *
 *
 
 	JPQL 소개
	• JPQL은 객체지향 쿼리 언어다.따라서 테이블을 대상으로 쿼리하는 것이 아니라 엔티티 객체를 대상으로 쿼리한다.
	• JPQL은 SQL을 추상화해서 특정데이터베이스 SQL에 의존하지 않는다.
	• JPQL은 결국 SQL로 변환된다.
	
	
	
	
 *
 */
public class JpaMainJpql {

	public static void main(String[] args) {
		벌크연산();
	}
	
	
	/**
	 * 
	 
	 	* 벌크 연산 : 여러건의 데이터를 한번에 수정하는 것
			• 재고가 10개 미만인 모든 상품의 가격을 10% 상승하게 하려면 어떻게 해야 할까?
			
			• JPA 변경 감지 기능으로 실행하려면 너무 많은 SQL 실행해야 한다.
				 1. 재고가 10개 미만인 상품을 리스트로 조회한다.
				 2. 상품 엔티티의 가격을 10% 증가한다.
				 3. 트랜잭션 커밋 시점에 변경감지가 동작한다.
			
			• 변경된 데이터가 100건이라면 100번의 UPDATE SQL 실행한다.


	 	* 벌크 연산 예제
			• 쿼리 한 번으로 여러 테이블 로우 변경(엔티티)할 수 있다.
			• executeUpdate()의 결과는 영향받은 엔티티 수를 반환한다.
			• JAP는 UPDATE, DELETE 지원한다.
			• INSERT(insert into .. select, 하이버네이트 지원)

	 	* 벌크 연산 주의
			• 벌크 연산은 영속성 컨텍스트를 무시하고 데이터베이스에 직접 쿼리한다.
			• 이로인해 실제 영속성 컨텍스트의 내용은 변경이 되지 않는 문제가 발생한다. 
			• 문제 해결 방법
				• 영속성 컨텍스트에 저장하는 로직 보다 벌크 연산을 먼저 실행
				• 벌크 연산 수행 후 영속성 컨텍스트 초기화
	 */
	public static void 벌크연산() {
		
		/**
		 * 애플리케이션 로딩 시점에 한번만 만든다. 데이터베이스당 1개만 실행한다.
		 * persistence.xml의 persistence-unit name 과 맵핑된다.
		 */
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		
		// 트랜젝션이 일어나는 시점에 계속 만들어 준다.
		EntityManager em = emf.createEntityManager();

		// JPA에서는 데이터 변경 관련 모든 작업은 트랙젝션 안에서 실행되어야 한다. 
		EntityTransaction tx = em.getTransaction();
		// 트랜잭션 시작
		tx.begin();
		
		try {
			
			Team team1 = new Team();
			team1.setName("팀A");
			em.persist(team1);
			
			Team team2 = new Team();
			team2.setName("팀B");
			em.persist(team2);
			
			Member member1 = new Member();
			member1.setName("회원1");
			member1.setAge(0);
			member1.changeTeam(team1);
			em.persist(member1);
			
			Member member2 = new Member();
			member2.setName("회원2");
			member2.setAge(0);
			member2.changeTeam(team2);
			em.persist(member2);
			
			Member member3 = new Member();
			member3.setName("회원3");
			member3.setAge(0);
			member3.changeTeam(team2);
			em.persist(member3);
			
			// 회원의 나이를 20으로 일괄 변경해라.
			String query = "update Member m set m.age = 20 ";
			
			/**
			 * 
			 * createQuery()가 실행되면 flush()가 실행 되고, update 쿼리를 날리게 된다.
			 * 이때 update 쿼리 실해은 영속성 컨텍스트를 거치지 않고 바로 DB에서 업데이트를 실행한다.
			 */
			int resultCnt = em.createQuery(query).executeUpdate();
					
			System.out.println("resultCnt : " + resultCnt);
			// 영속성 컨텍스트를 변경해 주지 않았기 때문에 아래 로그 출력 결과는 최초 0 값이 찍힌다.
			System.out.println("member1 age : " + member1.getAge());
			System.out.println("member2 age : " + member2.getAge());
			System.out.println("member3 age : " + member3.getAge());
			
			
			
			
			/**
			 * 벌크연산 후 영속성 컨텍스트를 사용하기 전에 초기화 해준다.
			 */
			
			em.clear();
			
			Member findMember =  em.find(Member.class, member1.getId());
			
			System.out.println("findMember : " + findMember.getAge());
			
			// 트랜잭션 종료
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
			e.printStackTrace();
			tx.rollback();
		} finally {
			// EntityManager가 DB connection을 물고 있기 때문에 꼭 닫아줘야 한다.
			em.close();
		}
		
		emf.close();
		
	}
	
	/**
	 * 
	 	* Named 쿼리 - 정적 쿼리
			• 미리 정의해서 이름을 부여해두고 사용하는 JPQL
			• 정적 쿼리
			
			• 어노테이션, XML에 정의
				• 어노테이션
				 	→ Entity 객체에 @NamedQuery 선언하여 사용함.
					→ @NamedQuery(name = "Member.findByUsername", query = "select m from Member m where m.name = :name")
			
				• XML
					→ persistence.xml에 설정 추가
						<persistence-unit name="hello">
							<mapping-file>META-INF/ormMember.xml</mapping-file>
						</persistence-unit>
					→ ormMember.xml 안에 SQL을 선언한다.	
						
			
			• 애플리케이션 로딩 시점에 초기화(캐싱됨) 후 재사용
				• 성능에 약간 메리트 있음.
			• 애플리케이션 로딩 시점에 쿼리를 검증
			
			
		* Named 쿼리 환경에 따른 설정
			• XML이 항상 우선권을 가진다.
			• 애플리케이션 운영 환경에 따라 다른 XML을 배포할 수 있다.
	
			
	 */
	public static void named쿼리() {
		
		/**
		 * 애플리케이션 로딩 시점에 한번만 만든다. 데이터베이스당 1개만 실행한다.
		 * persistence.xml의 persistence-unit name 과 맵핑된다.
		 */
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		
		// 트랜젝션이 일어나는 시점에 계속 만들어 준다.
		EntityManager em = emf.createEntityManager();

		// JPA에서는 데이터 변경 관련 모든 작업은 트랙젝션 안에서 실행되어야 한다. 
		EntityTransaction tx = em.getTransaction();
		// 트랜잭션 시작
		tx.begin();
		
		try {
			
			Team team1 = new Team();
			team1.setName("팀A");
			em.persist(team1);
			
			Team team2 = new Team();
			team2.setName("팀B");
			em.persist(team2);
			
			Member member1 = new Member();
			member1.setName("회원1");
			member1.changeTeam(team1);
			em.persist(member1);
			
			Member member2 = new Member();
			member2.setName("회원2");
			member2.changeTeam(team2);
			em.persist(member2);
			
			em.flush();
			em.clear();

			/**
			 * 엔티티 객체에 @NamedQuery 로 선언한 name 명을 createNamedQuery 메소드의 첫번째 인자에 맵핑해 준다.
			 */
			List<Member> members4 = em.createNamedQuery("Member.findByUsername", Member.class)
					.setParameter("name", "회원2")
					.getResultList();
			
			for (Member m : members4) {
				System.out.println("member : " + m);
			}
			
			// 트랜잭션 종료
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
			e.printStackTrace();
			tx.rollback();
		} finally {
			// EntityManager가 DB connection을 물고 있기 때문에 꼭 닫아줘야 한다.
			em.close();
		}
		
		emf.close();
		
		
	}
	
	/**
	 * 
	 * 
	 	* 엔티티 직접 사용 - 기본 키 값
			• JPQL에서 엔티티를 직접 사용하면 SQL에서 해당 엔티티의 기본 키 값을 사용
			
			• [JPQL]
					select count(m.id) from Member m 	// 엔티티의 아이디를 사용
					select count(m) from Member m 		// 엔티티를 직접 사용
			
			• [SQL](JPQL 둘다 같은 다음 SQL 실행)
					select count(m.id) as cnt from Member m

		* 엔티티 직접 사용 - 기본 키 값
			• select m from Member m where m = :member 		// 엔티티를 파라미터로 전달 
			• select m from Member m where m.id = :memberId 	// 식별자를 직접 전달
				→ 둘 다 실제 SQL는 동일하게 나간다.
				실행 SQL : 	select m.* from Member m where m.id=?
				
		* 엔티티 직접 사용 - 외래 키 값

	 */
	public static void 엔티티직접사용() {
		/**
		 * 애플리케이션 로딩 시점에 한번만 만든다. 데이터베이스당 1개만 실행한다.
		 * persistence.xml의 persistence-unit name 과 맵핑된다.
		 */
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		
		// 트랜젝션이 일어나는 시점에 계속 만들어 준다.
		EntityManager em = emf.createEntityManager();

		// JPA에서는 데이터 변경 관련 모든 작업은 트랙젝션 안에서 실행되어야 한다. 
		EntityTransaction tx = em.getTransaction();
		// 트랜잭션 시작
		tx.begin();
		
		try {
			
			Team team1 = new Team();
			team1.setName("팀A");
			em.persist(team1);
			
			Team team2 = new Team();
			team2.setName("팀B");
			em.persist(team2);
			
			Member member1 = new Member();
			member1.setName("회원1");
			member1.changeTeam(team1);
			em.persist(member1);
			
			Member member2 = new Member();
			member2.setName("회원2");
			member2.changeTeam(team2);
			em.persist(member2);
			
			em.flush();
			em.clear();

			/**
		 	* 엔티티 직접 사용 - 기본 키 값
		 		• 실제 SQL
		 			select count(m1_0.id) from Member m1_0
			 */
//			String query = "select count(m) from Member m";
//			String query = "select count(m.id) from Member m";
			
//			Long memberCount = em.createQuery(query, Long.class).getSingleResult();
//			
//			System.out.println("memberCount : " + memberCount);
			
			
			/**
		 	* 엔티티 직접 사용 - 기본 키 값
				• 엔티티를 파라미터로 전달
			 */
//			String query3 = "select m from Member m where m = :member";
//			List<Member> members1 = em.createQuery(query3, Member.class)
//				.setParameter("member", member1)
//				.getResultList();
//			
//			for (Member m : members1) {
//				System.out.println("member : " + m);
//			}
			
			/**
			 * 엔티티 직접 사용 - 기본 키 값
				• 식별자를 직접 전달
			 */
//			String query4 = "select m from Member m where m.id = :memberId";
//			List<Member> members2 = em.createQuery(query4, Member.class)
//					.setParameter("memberId", member2.getId())
//					.getResultList();
//			
//			for (Member m : members2) {
//				System.out.println("member : " + m);
//			}
			
			
			/**
			 * 엔티티 직접 사용 - 외래 키 값
			 	• 엔티티를 파라미터로 전달
			 */
//			String query5 = "select m from Member m where m.team = :team";
//			List<Member> members3 = em.createQuery(query5, Member.class)
//					.setParameter("team", team1)
//					.getResultList();
//			
//			for (Member m : members3) {
//				System.out.println("member : " + m);
//			}
			
			
			/**
			 * 엔티티 직접 사용 - 외래 키 값
			 	• 식별자를 직접 전달
			 */
			String query6 = "select m from Member m where m.team.id = :teamId";
			List<Member> members4 = em.createQuery(query6, Member.class)
					.setParameter("teamId", team2.getId())
					.getResultList();
			
			for (Member m : members4) {
				System.out.println("member : " + m);
			}
			
			// 트랜잭션 종료
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
			e.printStackTrace();
			tx.rollback();
		} finally {
			// EntityManager가 DB connection을 물고 있기 때문에 꼭 닫아줘야 한다.
			em.close();
		}
		
		emf.close();
		
		
	}
	
	
	/**
	 * 
	 * 
	 	* TYPE
			• 조회 대상을 특정 자식으로 한정
			• 예) Item 중에 Book, Movie를 조회해라
			• [JPQL]
					select i from Item i
					where type(i) IN (Book, Movie)
			• [SQL]
					select i from i
					where i.DTYPE in (‘B’, ‘M’)

		* TREAT(JPA 2.1)
			• 자바의 타입 캐스팅과 유사
			• 상속 구조에서 부모 타입을 특정 자식 타입으로 다룰 때 사용
			• FROM, WHERE, SELECT(하이버네이트 지원) 사용


	 */
	public static void 다형성쿼리() {
		/**
		 * 애플리케이션 로딩 시점에 한번만 만든다. 데이터베이스당 1개만 실행한다.
		 * persistence.xml의 persistence-unit name 과 맵핑된다.
		 */
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		
		// 트랜젝션이 일어나는 시점에 계속 만들어 준다.
		EntityManager em = emf.createEntityManager();

		// JPA에서는 데이터 변경 관련 모든 작업은 트랙젝션 안에서 실행되어야 한다. 
		EntityTransaction tx = em.getTransaction();
		// 트랜잭션 시작
		tx.begin();
		
		try {
			
			
			Book book = new Book();
			book.setStore("StoreA");
			book.setName("ItemA");
			em.persist(book);
			
			Book book1 = new Book();
			book1.setStore("StoreB");
			book1.setName("ItemB");
			em.persist(book1);
			
			
			em.flush();
			em.clear();

			/**
			 * Type
			 */
//			String query = "select i from Item i where type(i) IN (Book)";
//			Item findItem =   em.createQuery(query, Item.class).getSingleResult();
//			
//			System.out.println("findItem : " + findItem);
			
			/**
			 * Treat
			 */
			String query1 = "select i from Item i where treat(i as Book).store = 'StoreA' ";
			Item findItem1 =   em.createQuery(query1, Item.class).getSingleResult();
			
			System.out.println("findItem : " + findItem1);
			
			
			// 트랜잭션 종료
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
			e.printStackTrace();
			tx.rollback();
		} finally {
			// EntityManager가 DB connection을 물고 있기 때문에 꼭 닫아줘야 한다.
			em.close();
		}
		
		emf.close();
		
	}
	
	
	/**
	 * 
	 * 
	 	* 페치 조인의 특징과 한계
			• 페치 조인 대상에는 별칭을 줄 수 없다. 
				• 하이버네이트는 가능, 가급적 사용X
			
			• 둘 이상의 컬렉션은 페치 조인 할 수 없다. 
				•주 객체 안에 참조 컬력션 객체가 있고, 참조 컬력션 객체 안에 또 참조 컬렉션 객체가 있는 경우, 이들을 모두 조인하는 짓은 하지 말자.
					- 데이터가 잘 맞지 않는다. 
				EX. select t from Team t join fetch t.members m join fetch m.orders
			
			• 컬렉션을 페치 조인하면 페이징 API(setFirstResult, setMaxResults)를 사용할 수 없다. 
				• 일대일, 다대일 같은 단일 값 연관 필드들은 페치 조인해도 페이징 가능
				•	일대다, 다대다 같은 경우 페이지 되지 않는다.
					• 하이버네이트는 경고 로그를 남기고 메모리에서 페이징(매우 위험)
					- WARN: HHH90003004: firstResult/maxResults specified with collection fetch; applying in memory

			• JPA 설계 사상은 객체 그래프를 탐색한다는 건데, 이는 참조된 객체의 정보는 모두 조회한다라는 가정하고 설계가 되었다.
				그런데 패치 조인 대상에 별칭을 준 후 조건을 건다는건 부작용을 낳게 된다. 이상 동작이 생길 수 있는 리스크가 있다.
				Ex. "select t from Team t join fetch t.members m where m.name = '회원1'"
				
				
				
		* 페치 조인의 특징
			• 연관된 엔티티들을 SQL 한 번으로 조회가 가능하다. - 성능 최적화
			
			• 엔티티에 직접 적용하는 글로벌 로딩 전략보다 우선한다.
				• @OneToMany(fetch = FetchType.LAZY) // 글로벌 로딩 전략
			
			• 실무에서 글로벌 로딩 전략은 모두 지연 로딩을 적용하고 최적화가 필요한 곳은 페치 조인 적용하는것이 좋다.



	 	* 페치 조인 - 정리
			• 모든 것을 페치 조인으로 해결할 수 는 없음
			
			• 페치 조인은 객체 그래프를 유지할 때 사용하면 효과적
				• 객체 그래프는 참조된 모든 정보를 조회 한다.
			
			• 여러 테이블을 조인해서 엔티티가 가진 모양이 아닌 전혀 다른 결과를 내야 하면, 
				페치 조인 보다는 일반 조인을 사용하고 필요한 데이터들만 조회해서 DTO로 반환하는 것이 효과적

				
	 */
	public static void 패치조인한계() {
		/**
		 * 애플리케이션 로딩 시점에 한번만 만든다. 데이터베이스당 1개만 실행한다.
		 * persistence.xml의 persistence-unit name 과 맵핑된다.
		 */
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		
		// 트랜젝션이 일어나는 시점에 계속 만들어 준다.
		EntityManager em = emf.createEntityManager();

		// JPA에서는 데이터 변경 관련 모든 작업은 트랙젝션 안에서 실행되어야 한다. 
		EntityTransaction tx = em.getTransaction();
		// 트랜잭션 시작
		tx.begin();
		
		try {
			
			Team teamA = new Team();
			teamA.setName("팀A");
			em.persist(teamA);
			
			Team teamB = new Team();
			teamB.setName("팀B");
			em.persist(teamB);
			
			
			Member member1 = new Member("회원1",10);
			member1.setType(MemberType.ADMIN);
			member1.changeTeam(teamA);
			em.persist(member1);
			
			Member member2 = new Member("회원2",70);
			member2.setType(MemberType.USER);
			member2.changeTeam(teamA);
			em.persist(member2);
			
			Member member3 = new Member("회원3",70);
			member3.setType(MemberType.USER);
			member3.changeTeam(teamB);
			em.persist(member3);
			
			
			
			em.flush();
			em.clear();
			
			/**
			 * • 페치 조인 대상에는 별칭을 줄 수 없다. 
					• 하이버네이트는 가능, 가급적 사용X
			 */
//			String query1 = "select t from Team t join fetch t.members m where m.name = '회원1'";
//			
//			List<Team> teams =  em.createQuery(query1, Team.class).getResultList();
//			
//			System.out.println(" team size : " + teams.size());
//			
//			for (Team t : teams) {
//				System.out.println("Team : " + t.getName() + " / Team 회원 수 : " + t.getMembers().size());
//				
//				for (Member m : t.getMembers()) {
//					System.out.println("-> Member : " + m);
//				}
//			}
			
			/**
			 * 
 				• 컬렉션을 페치 조인하면 페이징 API(setFirstResult, setMaxResults)를 사용할 수 없다. 
					• 일대일, 다대일 같은 단일 값 연관 필드들은 페치 조인해도 페이징 가능
					•	일대다, 다대다 같은 경우 페이지 되지 않는다.
						• 하이버네이트는 경고 로그를 남기고 메모리에서 페이징(매우 위험)
						- WARN: HHH90003004: firstResult/maxResults specified with collection fetch; applying in memory
						- 실제 페이징 쿼리가 만들어지지 않는다.
			 * 
			 */
//			String query2 = "select t from Team t join fetch t.members m";
//			
//			List<Team> teams1 =  em.createQuery(query2, Team.class)
//					.setFirstResult(0)
//					.setMaxResults(1)
//					.getResultList();
//			
//			System.out.println(" team size : " + teams1.size());
//			
//			for (Team t : teams1) {
//				System.out.println("Team : " + t.getName() + " / Team 회원 수 : " + t.getMembers().size());
//				
//				for (Member m : t.getMembers()) {
//					System.out.println("-> Member : " + m);
//				}
//			}
			
			/**
			 * 컬렉션을 페치 조인하면 페이징 API 문제 해결 방법
			 * 
			 * 1. 조인 없이 참조 컬렉션 객체를 가지고 있는 객체 단일 조회
			 * 2. 그러면 N +1 문제가 발생한다. 그래서 이를 해결하기 위해 fetch 조인을 사용하면 되는데 
			 * 	문제는 컬렉션을 페치 조인하면 페이징 먹지 않기 때문에 
			 * 	이때 참조 컬렉션 필드 위에 @BatchSize(size = 100) 선언하여 해결한다. 
			 * 
			 * 참조 컬렉션 필드 위에 @BatchSize(size = 100) 선언 
			 * - 컬렉션 객체를 사이즈 만큼 한번에 IN 조건으로 조회한다.
			 * 
			
			 * global 옵션은 persistence.xml에 선언하면 된다.
			 	* <property name="hibernate.default_batch_fetch_size" value="100"/>
			 	
			 * 
			 */
			// 1. 단일 객체 조회 : 여기서는 페이징 잘 된다.
			String query3 = "select t from Team t";
			
			List<Team> teams2 =  em.createQuery(query3, Team.class)
					.setFirstResult(0)
					.setMaxResults(10)
					.getResultList();
			
			System.out.println(" team size : " + teams2.size());
			
			// 2. 여기서 N+1 문제 발생. 그래서 Team 객체 안에 members 컬렉션 위에 @BatchSize(size = 100) 선언.
			for (Team t : teams2) {
				System.out.println("Team : " + t.getName() + " / Team 회원 수 : " + t.getMembers().size());
				
				for (Member m : t.getMembers()) {
					System.out.println("-> Member : " + m);
				}
			}
			
			
			// 트랜잭션 종료
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
			e.printStackTrace();
			tx.rollback();
		} finally {
			// EntityManager가 DB connection을 물고 있기 때문에 꼭 닫아줘야 한다.
			em.close();
		}
		
		emf.close();
		
	}
	
	
	/**
	 * 
	 	* 컬렉션 페치 조인
			• 일대다 관계, 컬렉션 페치 조인
			• [JPQL]
				select 	t
				from 	Team t join fetch t.members
				where 	t.name = ‘팀A'
			• [SQL]
				select*	T.*, M.*
				from 	TEAM T
				inner join MEMBER M ON T.ID=M.TEAM_ID
				where T.NAME = '팀A'
				
				
				
		* 페치 조인과 DISTINCT
			•Hibernate ORM 6부터 하위 컬렉션을 조인할 때 동일한 상위 엔티티 참조를 필터링하기 위해 더 이상 JPQL 및 HQL에서 distinct를 사용할 필요가 없습니다 . 
				반환되는 엔티티의 복제본은 이제 항상 Hibernate에 의해 필터링됩니다.	
				
			- DB의 경우 Team과 Member가 1 : N  관계일 경우 join 하여 조회하게 되면 Team의 사이즈와는 별개로 Member의 사이즈에 영향을 받는다.
			- 그래서 백단에서 조회시 동일한 결과를 얻게 되는데, Hibernate 에서는 중복되는 객체를 필터링 해준다.  물론 DB는 여전히 뻥튀기 되서 조회된다.
			  (과거에는 조회 시 중복을 제거하기 위해서 distinct를 해줘야 했음)
			  
			** 참고
			- DB에서는 distinct가 적용되려면 모든 조회 커럶이 일치해야 한다.
			  Ex. select distinct t.id, t.name, m.id, m.age from team t join member m on m.TEAM_ID = t.ID;
			  ->위에서 조회 한 "t.id, t.name, m.id, m.age"이 컬럼 모두가 일치해야 중복이 제거됨.
	 * 
	 */
	public static void 컬렉션패치조인() {
		/**
		 * 애플리케이션 로딩 시점에 한번만 만든다. 데이터베이스당 1개만 실행한다.
		 * persistence.xml의 persistence-unit name 과 맵핑된다.
		 */
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		
		// 트랜젝션이 일어나는 시점에 계속 만들어 준다.
		EntityManager em = emf.createEntityManager();

		// JPA에서는 데이터 변경 관련 모든 작업은 트랙젝션 안에서 실행되어야 한다. 
		EntityTransaction tx = em.getTransaction();
		// 트랜잭션 시작
		tx.begin();
		
		try {
			
			Team teamA = new Team();
			teamA.setName("팀A");
			em.persist(teamA);
			
			Team teamB = new Team();
			teamB.setName("팀B");
			em.persist(teamB);
			
			
			Member member1 = new Member("화원1",10);
			member1.setType(MemberType.ADMIN);
			member1.changeTeam(teamA);
			em.persist(member1);
			
			Member member2 = new Member("회원2",70);
			member2.setType(MemberType.USER);
			member2.changeTeam(teamA);
			em.persist(member2);
			
			Member member3 = new Member("회원3",70);
			member3.setType(MemberType.USER);
			member3.changeTeam(teamB);
			em.persist(member3);
			
			
			
			em.flush();
			em.clear();
			
			
			String query3 = "select t from Team t join fetch t.members m";
			
			List<Team> teams =  em.createQuery(query3, Team.class).getResultList();
			
			
			for (Team t : teams) {
				System.out.println("Team : " + t.getName() + " / Team 회원 수 : " + t.getMembers().size());
				
				for (Member m : t.getMembers()) {
					System.out.println("-> Member : " + m);
				}
			}
			
			
			// 트랜잭션 종료
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
			e.printStackTrace();
			tx.rollback();
		} finally {
			// EntityManager가 DB connection을 물고 있기 때문에 꼭 닫아줘야 한다.
			em.close();
		}
		
		emf.close();
		
		
	}
	
	/**
	 * 
	 
	 	* 페치 조인(fetch join)
			• SQL 조인 종류가 아니다.
			• JPQL에서 성능 최적화를 위해 제공하는 기능이다.
			• 연관된 엔티티나 컬렉션을 SQL 한 번에 함께 조회하는 기능이다.
			• join fetch 명령어 사용
				• 페치 조인 ::= [ LEFT [OUTER] | INNER ] JOIN FETCH 조인경로

		* 엔티티 페치 조인
			• 회원을 조회하면서 연관된 팀도 함께 조회(SQL 한 번에)
			• SQL을 보면 회원 뿐만 아니라 팀(T.*)도 함께 SELECT
			• [JPQL]
					select m from Member m join fetch m.team
			• [SQL]
					SELECT M.*, T.* FROM MEMBER M
					INNER JOIN TEAM T ON M.TEAM_ID=T.I


	 	* 페치 조인과 일반 조인의 차이
			• JPQL은 결과를 반환할 때 연관관계 고려X
			• 단지 SELECT 절에 지정한 엔티티만 조회할 뿐
			• 여기서는 팀 엔티티만 조회하고, 회원 엔티티는 조회X
			• 페치 조인을 사용할 때만 연관된 엔티티도 함께 조회(즉시 로딩) 
			• 페치 조인은 객체 그래프를 SQL 한번에 조회하는 개념

	 */
	public static void 엔티티패치조인() {
		/**
		 * 애플리케이션 로딩 시점에 한번만 만든다. 데이터베이스당 1개만 실행한다.
		 * persistence.xml의 persistence-unit name 과 맵핑된다.
		 */
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		
		// 트랜젝션이 일어나는 시점에 계속 만들어 준다.
		EntityManager em = emf.createEntityManager();

		// JPA에서는 데이터 변경 관련 모든 작업은 트랙젝션 안에서 실행되어야 한다. 
		EntityTransaction tx = em.getTransaction();
		// 트랜잭션 시작
		tx.begin();
		
		try {
			
			Team teamA = new Team();
			teamA.setName("팀A");
			em.persist(teamA);
			
			Team teamB = new Team();
			teamB.setName("팀B");
			em.persist(teamB);
			
			
			Member member1 = new Member("화원1",10);
			member1.setType(MemberType.ADMIN);
			member1.changeTeam(teamA);
			em.persist(member1);
			
			Member member2 = new Member("회원2",70);
			member2.setType(MemberType.USER);
			member2.changeTeam(teamA);
			em.persist(member2);
			
			Member member3 = new Member("회원2",70);
			member3.setType(MemberType.USER);
			member3.changeTeam(teamB);
			em.persist(member3);
			
			
			
			em.flush();
			em.clear();
			
			
			/**
			 * 일반 조회(조인X)
			 */
//			String query1 = "select m from Member m";
//			
//			List<Member> members =  em.createQuery(query1, Member.class).getResultList();
//			
//			for (Member m : members) {
//				System.out.println("Member : " + m);
//				
//				/**
//				 * 
//				 * 
//				 * 팀 조회 시 loop를 돌면서 일어나는 일
//			 		- 회원1의 팀 조회 시 : DB에서 조회
//					- 회원2의 팀 조회 시 : 1차캐시에서 조회
//						- 회원2의 경우 회원1과 같은 팀 소속이기 때문에 회원1 조회시 팀 정보가 이미 영속성 컨텍스트의 1차캐시에 저장되어 있다.
//					
//					- 회원3(팀B) 조회(DB에서 조회)
//				 */ 
//				System.out.println("Member.Tema.name: " + m.getTeam().getName());
//			}
			
			
			/**
			 * 
			 
			 	* 일반 조인
					 - 오직 JPQL에서 '조회의 주체가 되는 Entity만 조회하여 영속화' 하기 때문에 조인 된 테이블은 별도로 각각 다시 조회하게 된다.
					 
					 - 아래 SQL을 보면 알수 있듯이 실제 조회 쿼리는 join 쿼리 이지만 조회결과는 m.* 만 리턴하고 있다.
					 	select m.* from Member m join Team t on t.id=m.TEAM_ID
					 
					 - 따라서 Team을 조회 시 N +1의 문제는 그대로 발생하게 된다.  
					 
					 
			 * 
			 */
//			String query2 = "select m from Member m join team t";
//			
//			List<Member> members2 =  em.createQuery(query2, Member.class).getResultList();
//			
//			for (Member m : members2) {
//				System.out.println("Member : " + m);
//				
//				System.out.println("Member.Tema.name: " + m.getTeam().getName());
//			}
			
			
			/**
			 * 
			 
			 	* Fetch 조인
					 - 조회 주체가 되는 Entity와 조인 된 테이블 모두 영속화 한다.(영속성 컨텍스트에 저장)
					 
					 - 아래 SQL를 보면 알수 있듯이 조회결과에 member와 team 객체 정보 모두를 리턴하고 있다.(실제 쿼리는 m.*이 아니고 컬럼 전부를 나열한다)
					 	select m.*, t.* from Member m join Team t on t.id=m.TEAM_ID
					 	
					 - 따라서 N +1의 문제가 발생하지 않는다.  
					 
					 - 지연로딩 설정을 했어도 Fetch 조인이 우선순위가 높다. 
					 	(fetch = FetchType.LAZY)
					 	
					 *** 참고로 즉시로딩을 했다고 해서 N+1이 해결되는게 아니다. 
					 * 즉시 로딩과, 지연 로딩은 참조객체를 언제 가지고  오느냐(조회 하느냐)의 문제지 N+1은 그대로 발생한다.	
			 * 
			 */
			String query3 = "select m from Member m join fetch team t";
			
			List<Member> members3 =  em.createQuery(query3, Member.class).getResultList();
			
			for (Member m : members3) {
				System.out.println("Member : " + m);
				
				System.out.println("Member.Tema.name: " + m.getTeam().getName());
			}
			
			
			
			
			// 트랜잭션 종료
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
			e.printStackTrace();
			tx.rollback();
		} finally {
			// EntityManager가 DB connection을 물고 있기 때문에 꼭 닫아줘야 한다.
			em.close();
		}
		
		emf.close();
		
		
	}
	
	
	/**
	 * 
	 * 
	 	
	 *	경로 표현식
		• .(점)을 찍어 객체 그래프를 탐색하는 것
			select 	m.username -> 상태 필드
			from 	Member m 
			join 		m.team t -> 단일 값 연관 필드
			join 		m.orders o -> 컬렉션 값 연관 필드
			where 	t.name = '팀A
			
			
	 *	경로 표현식 용어 정리
		• 상태 필드(state field): 단순히 값을 저장하기 위한 필드
 			(ex: m.username)
		
		• 연관 필드(association field): 연관관계를 위한 필드
			• 단일 값 연관 필드: 
				@ManyToOne, @OneToOne, 대상이 엔티티(ex: m.team)
			
			• 컬렉션 값 연관 필드:
				@OneToMany, @ManyToMany, 대상이 컬렉션(ex: m.orders)
	
	
	 * 경로 표현식 특징
		• 상태 필드(state field): 경로 탐색의 끝이다, 더이상 탐색이 되지 않는다.
			ex. 
				JPQL: select m.username, m.age from Member m)
				SQL: select m.username, m.age from Member m
				
		• 단일 값 연관 경로: 묵시적 내부 조인(inner join) 발생한다, 추가 단색이 가능하다 (ex. m.team.name)
			ex.
				JPQL: select o.member from Order o
				SQL: select m.* from Orders o inner join Member m on o.member_id = m.id
				
		• 컬렉션 값 연관 경로: 묵시적 내부 조인 발생한다, 어디상 탐색이 되지 않는다.
			• FROM 절에서 명시적 조인을 통해 별칭을 얻으면 별칭을 통해 탐색 가능
		
		
	 * 명시직 조인, 묵시적 조인
		• 명시적 조인: join 키워드 직접 사용
			• select m from Member m join m.team t
		
		• 묵시적 조인: 경로 표현식에 의해 묵시적으로 SQL 조인 발생 (내부 조인만 가능)
			• select m.team from Member m


	 * 경로 표현식 - 예제
	 	•단일 값 연관 필드
			• select o.member.team from Order o -> 성공
		
		• 컬렉션 값 연관 필드:
			• select t.members from Team -> 성공
				• select t.members.username from Team t -> 실패 : 추가 탐색이 안된다.
					• select m.username from Team t join t.members m -> 성공 : 추가 탐색을 하고자 하는 경우 명시적 조인으로 변경해야 한다.


	 * 경로 탐색을 사용한 묵시적 조인 시 주의사항
		• 항상 내부 조인
		• 컬렉션은 경로 탐색의 끝, 명시적 조인을 통해 별칭을 얻어야함
		• 경로 탐색은 주로 SELECT, WHERE 절에서 사용하지만 묵시적 조인으로 인해 SQL의 FROM (JOIN) 절에 영향을 줌



	 *** 실무 조언 ***

		• 가급적 묵시적 조인 대신에 명시적 조인 사용
		
		• 조인은 SQL 튜닝에 중요 포인트
		
		• 묵시적 조인은 조인이 일어나는 상황을 한눈에 파악하기 어려움

	 */
	public static void 경로표현식() {
		/**
		 * 애플리케이션 로딩 시점에 한번만 만든다. 데이터베이스당 1개만 실행한다.
		 * persistence.xml의 persistence-unit name 과 맵핑된다.
		 */
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		
		// 트랜젝션이 일어나는 시점에 계속 만들어 준다.
		EntityManager em = emf.createEntityManager();

		// JPA에서는 데이터 변경 관련 모든 작업은 트랙젝션 안에서 실행되어야 한다. 
		EntityTransaction tx = em.getTransaction();
		// 트랜잭션 시작
		tx.begin();
		
		try {
			
			Team team = new Team();
			team.setName("teamA");
			em.persist(team);
			
			
			Member member = new Member("member1",10);
			member.setType(MemberType.ADMIN);
			member.changeTeam(team);
			em.persist(member);
			
			Member member2 = new Member("member2",70);
			member2.setType(MemberType.USER);
			member2.changeTeam(team);
			em.persist(member2);
			
			
			
			em.flush();
			em.clear();
			
			/**
			 *	경로표현식 - 단일 값 연관관계 필드 
			 *	- 추가 탐색 가능
			 *	- 묵시적 내부 조인 발생 
			 */
			
			String query1 = "select m.team from Member m";
			
			List<Team> teamList =  em.createQuery(query1, Team.class).getResultList();
			
			for (Team t : teamList) {
				System.out.println("Team : " + t);
			}
			
			
			/**
			 * 
			 * 경로표현식 - 컬렉션 연관관계 필드(추가 탐색 안되는 버전)
			 *	- 추가탐색 불가능 (ex. t.members.name <- 안된다)
			 *	- 묵시적 내부 조인 발생 
			 */
			String query2 = "select t.members.name from Team t";
			
			List<Collection> cList =  em.createQuery(query2, Collection.class).getResultList();
			
			for (Object s : cList) {
				System.out.println("strings1 : " + s);
			}
			
			/**
			 * 
			 * 경로표현식 - - 컬렉션 연관관계 필드 (추가 탐색 되는 버전)
			 * - 추가탐색 불가능 (ex. t.members.name <- 안된다)
			 * - 추가 탐색을 하기 위해서는 명시적 조인을 해야 한다.
			 */
			String query3 = "select m.name from Team t join t.members m";
			
			List<String> result =  em.createQuery(query3, String.class).getResultList();
			
			for (String s : result) {
				System.out.println("strings1 : " + s);
			}
			
			
			
			
			
			// 트랜잭션 종료
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
			e.printStackTrace();
			tx.rollback();
		} finally {
			// EntityManager가 DB connection을 물고 있기 때문에 꼭 닫아줘야 한다.
			em.close();
		}
		
		emf.close();
		
		
	}
	
	/**
	 * 
	 * 
	 	사용자 정의 함수 호출
	• 하이버네이트는 사용전 방언에 추가해야 한다.
	• 사용하는 DB 방언을 상속받고, 사용자 정의 함수를 등록한다.
		- select function('group_concat', i.name) from Item i
		
		
		hibernate 버전에 따라 강의 영상대로 실행되지 않음.

	 */
	public static void 사용자정의함수() {
		/**
		 * 애플리케이션 로딩 시점에 한번만 만든다. 데이터베이스당 1개만 실행한다.
		 * persistence.xml의 persistence-unit name 과 맵핑된다.
		 */
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		
		// 트랜젝션이 일어나는 시점에 계속 만들어 준다.
		EntityManager em = emf.createEntityManager();

		// JPA에서는 데이터 변경 관련 모든 작업은 트랙젝션 안에서 실행되어야 한다. 
		EntityTransaction tx = em.getTransaction();
		// 트랜잭션 시작
		tx.begin();
		
		try {
			
			Team team = new Team();
			team.setName("teamA");
			em.persist(team);
			
			Team team1 = new Team();
			team1.setName("teamB");
			em.persist(team1);
			
			
			Member member = new Member("member1",10);
			member.setType(MemberType.ADMIN);
			member.changeTeam(team);
			em.persist(member);
			
			Member member2 = new Member("member2",70);
			member2.setType(MemberType.USER);
			member2.changeTeam(team1);
			em.persist(member2);
			
			
			
			em.flush();
			em.clear();
			
			String query1 = "select function('group_concat', m.name) from Member m";
			
			List<String> strings1 =  em.createQuery(query1, String.class).getResultList();
			
			for (String s : strings1) {
				System.out.println("strings1 : " + s);
			}
			
			
			// 트랜잭션 종료
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
			e.printStackTrace();
			tx.rollback();
		} finally {
			// EntityManager가 DB connection을 물고 있기 때문에 꼭 닫아줘야 한다.
			em.close();
		}
		
		emf.close();
	}
	
	
	/**
	 * 
	 * 
	 	JPQL 기본 함수
		• CONCAT
		• SUBSTRING
		• TRIM
		• LOWER, UPPER
		• LENGTH
		• LOCATE
		• ABS, SQRT, MOD
		• SIZE, INDEX(JPA 용도)
	 */
	public static void 기본함수() {
		/**
		 * 애플리케이션 로딩 시점에 한번만 만든다. 데이터베이스당 1개만 실행한다.
		 * persistence.xml의 persistence-unit name 과 맵핑된다.
		 */
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		
		// 트랜젝션이 일어나는 시점에 계속 만들어 준다.
		EntityManager em = emf.createEntityManager();

		// JPA에서는 데이터 변경 관련 모든 작업은 트랙젝션 안에서 실행되어야 한다. 
		EntityTransaction tx = em.getTransaction();
		// 트랜잭션 시작
		tx.begin();
		
		try {
			
			Team team = new Team();
			team.setName("teamA");
			em.persist(team);
			
			Team team1 = new Team();
			team1.setName("teamB");
			em.persist(team1);
			
			
			Member member = new Member("member1",10);
			member.setType(MemberType.ADMIN);
			member.changeTeam(team);
			em.persist(member);
			
			Member member2 = new Member("member2",70);
			member2.setType(MemberType.USER);
			member2.changeTeam(team1);
			em.persist(member2);
			
			
			
			em.flush();
			em.clear();
			
			String query1 = "select SIZE(t.members) from Team t";
			
			List<Integer> strings1 =  em.createQuery(query1, Integer.class).getResultList();
			
			for (Integer s : strings1) {
				System.out.println("strings1 : " + s);
			}
			
			
			// 트랜잭션 종료
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
			e.printStackTrace();
			tx.rollback();
		} finally {
			// EntityManager가 DB connection을 물고 있기 때문에 꼭 닫아줘야 한다.
			em.close();
		}
		
		emf.close();
		
	}
	
	
	/**
	 * 
	 * 1. 일반 CASE 식
	 * 2. 단순 CASE 식
	 * 3. COALESCE : 하나씩 조회해서 null이 아니면 반환
	 * 4. NULLIF : 두 값이 같으면 null 반환, 다르면 첫번째 값 반환
	 * 
	 */
	public static void 조건식_CASE() {
		
		/**
		 * 애플리케이션 로딩 시점에 한번만 만든다. 데이터베이스당 1개만 실행한다.
		 * persistence.xml의 persistence-unit name 과 맵핑된다.
		 */
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		
		// 트랜젝션이 일어나는 시점에 계속 만들어 준다.
		EntityManager em = emf.createEntityManager();

		// JPA에서는 데이터 변경 관련 모든 작업은 트랙젝션 안에서 실행되어야 한다. 
		EntityTransaction tx = em.getTransaction();
		// 트랜잭션 시작
		tx.begin();
		
		try {
			
			Team team = new Team();
			team.setName("teamA");
			em.persist(team);
			
			Team team1 = new Team();
			team1.setName("teamB");
			em.persist(team1);
			
			
			Member member = new Member("member1",10);
			member.setType(MemberType.ADMIN);
			member.changeTeam(team);
			em.persist(member);
			
			Member member2 = new Member("member2",70);
			member2.setType(MemberType.USER);
			member2.changeTeam(team1);
			em.persist(member2);
			
			
			
			em.flush();
			em.clear();
			
			/**
			 * 1, 기본 CASE 식
			 * 
			 *  추가. 스칼라 타입인데 객체가 포함된 경우.
			 * - case when 으로 alias 하나 조회하고, Member 객체도 전체 조회한 경우
			 */
			String query = "select "
					+"case when m.age <= 10 then '학생요금' "
					+"when m.age >= 60 then '경로요금' "
					+"else '일반요금' end  as count, m "
					+"from Member m";
			
			List<Object[]> objectList =  em.createQuery(query).getResultList();
			
			for (Object[] objectArr : objectList) {
				for (Object object : objectArr) {
					System.out.println("strings : " + object);
					
					// Member 객체인 경우 Member 객체로 캐스팅
					if(object instanceof Member) {
						Member m = (Member)object;
						System.out.println(m.getAge());
						System.out.println(m.getName());
						System.out.println(m.getTeam().getName());
					}
				}
			}
			
			
			/**
			 * 2. 단순 CASE 식
			 */
			String query1 = "select "
					+"case t.name "
					+"		when 'teamA' then '인센티브110%' "
					+"		when 'teamB' then '인센티브150%' "
					+"		else '인센티브 없음' end "
					+"from Team t";
			
			List<String> strings =  em.createQuery(query1, String.class).getResultList();
			
			for (String s : strings) {
				System.out.println("strings : " + s);
			}
			
			/**
			 * 
			 
			 	3. COALESCE  
			 		- 첫번째 인자에 비교할 식별자를 하나씩 비교해서 null이 아니면 첫번째 인자 값 반환하고, Null 이면 두번째 인자 값 반환한다.
				 	- COALESCE(m.name, '이름 없는 회원')
			    
			 */
			String query2 = "select COALESCE(m.name, '이름 없는 회원') from Member m";
			
			List<String> strings2 =  em.createQuery(query2, String.class).getResultList();
			
			for (String s : strings2) {
				System.out.println("strings2 : " + s);
			}
			
			
			/**
			 * 
			 
			 	 4. NULLIF
			 	 	- 첫번째 인자에 비교할 식별자를 넣고, 두번째 인자에 첫번째 인자와 비교할 값을 넣은 후 두 값이 같으면 null 반환, 다르면 첫번째 인자 값 반환
			   
			 */
			String query3 = "select NULLIF(m.name, 'member1') from Member m";
			
			List<String> strings3 =  em.createQuery(query3, String.class).getResultList();
			
			for (String s : strings3) {
				System.out.println("strings3 : " + s);
			}
			
			
			// 트랜잭션 종료
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
			e.printStackTrace();
			tx.rollback();
		} finally {
			// EntityManager가 DB connection을 물고 있기 때문에 꼭 닫아줘야 한다.
			em.close();
		}
		
		emf.close();
		
		
	}
	
	/**
	 * 
	 * 
	 	JPQL 타입 표현
		• 문자: ‘HELLO’, ‘She’’s’
		• 숫자: 10L(Long), 10D(Double), 10F(Float)
		• Boolean: TRUE, FALSE
		• ENUM: jpql.MemberType.Admin (패키지명 포함)
		• 엔티티 타입: TYPE(m) = Member (상속 관계에서 사용)

		JPQL 기타
		• SQL과 문법이 같은 식
		• EXISTS, IN
		• AND, OR, NOT
		• =, >, >=, <, <=, <>
		• BETWEEN, LIKE, IS NULL

	 */
	public static void 타입표현() {
		
		/**
		 * 애플리케이션 로딩 시점에 한번만 만든다. 데이터베이스당 1개만 실행한다.
		 * persistence.xml의 persistence-unit name 과 맵핑된다.
		 */
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		
		// 트랜젝션이 일어나는 시점에 계속 만들어 준다.
		EntityManager em = emf.createEntityManager();

		// JPA에서는 데이터 변경 관련 모든 작업은 트랙젝션 안에서 실행되어야 한다. 
		EntityTransaction tx = em.getTransaction();
		// 트랜잭션 시작
		tx.begin();
		
		try {
			
			Team team = new Team();
			team.setName("teamA");
			em.persist(team);
			
			
			Member member = new Member("member1",10);
			member.setType(MemberType.ADMIN);
			member.changeTeam(team);
			em.persist(member);
			
			
			
			em.flush();
			em.clear();
			
			// enum을 사용하는 경우 하드코딩 하는 경우 패키지 명을 다 넣어야 한다.
			String query1 = "select m.name, 'HELLO', true from Member m"
					+ " where m.type = jpql.MemberType.ADMIN";
			
			// enum 부분을 하드코딩 하지 않고 파라미터 바인딩으로 처리
			String query2 = "select m.name, 'HELLO', true from Member m"
					+ " where m.type = :type";
			List<Object[]> resultList = em.createQuery(query2)
					.setParameter("type", MemberType.ADMIN)
					.getResultList();
			
			for (Object[] objectArr : resultList) {
				System.out.println("objects : " + objectArr[0]);
				System.out.println("objects : " + objectArr[1]);
				System.out.println("objects : " + objectArr[2]);
			}
			
//			for (Object[] objectArr : resultList) {
//				for (Object o : objectArr) {
//					System.out.println("objects : " + o);
//				}
//			}
			
			
			Book book = new Book();
			book.setName("Funny");
			book.setStore("경기");
			em.persist(book);
			
			/**
			 * 상속관계에서 where 조건에 type(부모객체) = 자식객체 이와같이 넣어주면
			 * 여러 자식 객체 중 맵핑 된 자식객체를 조회해 온다. 
			 */
			List<Book> books = em.createQuery("select i from Item i where type(i) = Book", Book.class)
				.getResultList();
			
			for (Book item : books) {
				System.out.println("item book : " + item.getName());
				System.out.println("item book : " + item.getStore());
			}
			
			// 트랜잭션 종료
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
			e.printStackTrace();
			tx.rollback();
		} finally {
			// EntityManager가 DB connection을 물고 있기 때문에 꼭 닫아줘야 한다.
			em.close();
		}
		
		emf.close();
		
		
	}
	
	
	/**
	 * 
	 * 
	 	서브 쿼리 지원 함수
		• [NOT] EXISTS (subquery): 서브쿼리에 결과가 존재하면 참
		
		• {ALL | ANY | SOME} (subquery)
			• ALL 모두 만족하면 참
			• ANY, SOME: 같은 의미, 조건을 하나라도 만족하면 참
			
		• [NOT] IN (subquery): 서브쿼리의 결과 중 하나라도 같은 것이 있으면 참
	
	 */
	public static void 서브쿼리() {
		/**
		 * 애플리케이션 로딩 시점에 한번만 만든다. 데이터베이스당 1개만 실행한다.
		 * persistence.xml의 persistence-unit name 과 맵핑된다.
		 */
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		
		// 트랜젝션이 일어나는 시점에 계속 만들어 준다.
		EntityManager em = emf.createEntityManager();

		// JPA에서는 데이터 변경 관련 모든 작업은 트랙젝션 안에서 실행되어야 한다. 
		EntityTransaction tx = em.getTransaction();
		// 트랜잭션 시작
		tx.begin();
		
		try {
			
			Team team = new Team();
			team.setName("teamA");
			em.persist(team);
			
			Team team1 = new Team();
			team1.setName("teamB");
			em.persist(team1);
			
			Member member = new Member("member1",10);
			member.changeTeam(team);
			em.persist(member);
			
			Member member2 = new Member("member2",15);
			member2.changeTeam(team1);
			em.persist(member2);
			
			Member member3 = new Member("member3",20);
			member3.changeTeam(team);
			em.persist(member3);
			
			Member member4 = new Member("member4",30);
			member4.changeTeam(team1);
			em.persist(member4);
			
			Member member5 = new Member("member5",14);
			em.persist(member5);
			
			
			em.flush();
			em.clear();
			
			/**
			 * 
			 	[NOT] EXISTS (subquery): 서브쿼리에 결과가 존재하면 참
			 */
//			String query1 = "select m from Member m where exists (select t from m.team t where t.name = 'teamB')";
//			List<Member> findMembers1 = em.createQuery(query1, Member.class)
//					.getResultList();
//			
//			System.out.println("findMembers1 size : " + findMembers1.size());
//			
//			for (Member m1 : findMembers1) {
//				System.out.println("findMembers1 member " + m1);
//			}
			
			/**
			 * 
			  	{ALL | ANY | SOME} (subquery)
					• ALL 모두 만족하면 참
			 */
//			String query2 = "select m from Member m where m.age >  all (select avg(m.age) from Member m)";
//			List<Member> findMembers2 = em.createQuery(query2, Member.class)
//					.getResultList();
//			
//			System.out.println("findMembers2 size : " + findMembers2.size());
//			
//			for (Member m2 : findMembers2) {
//				System.out.println("findMembers2 member " + m2);
//			}
			
			
			/**
			 * 
			 * 
			 	{ALL | ANY | SOME} (subquery)
					• ANY, SOME: 같은 의미, 조건을 하나라도 만족하면 참
			 */
//			String query3 = "select m from Member m where m.team =  some (select t from Team t)";
//			List<Member> findMembers3 = em.createQuery(query3, Member.class)
//					.getResultList();
//			
//			System.out.println("findMembers3 size : " + findMembers3.size());
//			
//			for (Member m3 : findMembers3) {
//				System.out.println("findMembers3 member " + m3);
//			}
			
			/**
			 * 
			 * [NOT] IN (subquery): 서브쿼리의 결과 중 하나라도 같은 것이 있으면 참
			 */
			String query4 = "select m from Member m where m.team not in (select t from Team t where t.name = 'teamB')";
			List<Member> findMembers4 = em.createQuery(query4, Member.class)
					.getResultList();
			
			System.out.println("findMembers4 size : " + findMembers4.size());
			
			for (Member m4 : findMembers4) {
				System.out.println("findMembers4 member " + m4);
			}
			
			
			
			// 트랜잭션 종료
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
			tx.rollback();
		} finally {
			// EntityManager가 DB connection을 물고 있기 때문에 꼭 닫아줘야 한다.
			em.close();
		}
		
		emf.close();
		
	}
	
	public static void 조인() {
		
		/**
		 * 애플리케이션 로딩 시점에 한번만 만든다. 데이터베이스당 1개만 실행한다.
		 * persistence.xml의 persistence-unit name 과 맵핑된다.
		 */
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		
		// 트랜젝션이 일어나는 시점에 계속 만들어 준다.
		EntityManager em = emf.createEntityManager();

		// JPA에서는 데이터 변경 관련 모든 작업은 트랙젝션 안에서 실행되어야 한다. 
		EntityTransaction tx = em.getTransaction();
		// 트랜잭션 시작
		tx.begin();
		
		try {
			
			Team team = new Team();
			team.setName("teamA");
			em.persist(team);
			
			Team team1 = new Team();
			team1.setName("teamB");
			em.persist(team1);
			
			Member member = new Member();
			member.setName("member1");
			member.setComp("crizen1");
			member.setAge(10);
			member.changeTeam(team);
			em.persist(member);
			
			Member member2 = new Member();
			member2.setName("member2");
			member2.setComp("crizen2");
			member2.setAge(13);
			member2.changeTeam(team1);
			em.persist(member2);
			
			
			em.flush();
			em.clear();
			
			/**
			 * inner join : 내부조인
			 */
			String query1 = "select m from Member m inner join m.team t";
			List<Member> findMembers1 = em.createQuery(query1, Member.class)
					.getResultList();
			
			/**
			 * left outer join : 외부조인
			 */
			String query2 = "select m from Member m left outer join m.team t";
			List<Member> findMembers2 = em.createQuery(query2, Member.class)
					.getResultList();
			
			for (Member m2 : findMembers2) {
				System.out.println("findMembers2 team : " + m2.getTeam().getName());
			} 
			
			/**
			 * 세타조인 : 연관관계 없는 테이블 간 조인(일명 막 조인)
			 */
			String query3 = "select count(m) from Member m, Team t where m.name = t.name";
			List<Member> findMembers3 = em.createQuery(query3, Member.class)
					.getResultList();
			
			
			/**
			 * 조인 대상 필터링
			 */
			String query4 = "select m from Member m join m.team t on t.name = 'teamB'";
			List<Member> findMembers4 = em.createQuery(query4, Member.class)
					.getResultList();
			
			System.out.println("findMembers4 size : " + findMembers4.size());
			for (Member m4 : findMembers4) {
				System.out.println("findMembers4 member name : " + m4.getName());
				System.out.println("findMembers4 team name : " + m4.getTeam().getName());
			}
			
			/**
			 * 연관관계 없는 엔티티 외부 조인
			 */
			String query5 = "select m from Member m left join Team t on m.name = t.name";
			List<Member> findMembers5 = em.createQuery(query5, Member.class)
					.getResultList();
			
			System.out.println("findMembers5 size : " + findMembers5.size());
			for (Member m5 : findMembers5) {
				System.out.println("findMembers5 member name : " + m5.getName());
				System.out.println("findMembers5 team name : " + m5.getTeam().getName());
			}
			
			// 트랜잭션 종료
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} finally {
			// EntityManager가 DB connection을 물고 있기 때문에 꼭 닫아줘야 한다.
			em.close();
		}
		
		emf.close();
		
		
	}
	
	
	/**
	 * 
	 *
	  	페이징 API
		• JPA는 페이징을 다음 두 API로 추상화
		• setFirstResult(int startPosition) : 조회 시작 위치
			(0부터 시작)
		• setMaxResults(int maxResult) : 조회할 데이터 수

	 */
	public static void 페이징() {
		/**
		 * 애플리케이션 로딩 시점에 한번만 만든다. 데이터베이스당 1개만 실행한다.
		 * persistence.xml의 persistence-unit name 과 맵핑된다.
		 */
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		
		// 트랜젝션이 일어나는 시점에 계속 만들어 준다.
		EntityManager em = emf.createEntityManager();

		// JPA에서는 데이터 변경 관련 모든 작업은 트랙젝션 안에서 실행되어야 한다. 
		EntityTransaction tx = em.getTransaction();
		// 트랜잭션 시작
		tx.begin();
		
		try {
			
			Team team = new Team();
			team.setName("teamA");
			em.persist(team);
			
			for (int i = 0; i < 100; i++) {
				Member member = new Member();
				member.setName("member"+i);
				member.setComp("crizen");
				member.setAge(i);
				member.changeTeam(team);
				em.persist(member);
			}
			
			
			em.flush();
			em.clear();
			
			List<Member> findMembers = em.createQuery("select m from Member m order by m.age desc", Member.class)
					.setFirstResult(0)
					.setMaxResults(10)
					.getResultList();
			
			System.out.println("findMembers size : " + findMembers.size());
			
			for (Member findMember : findMembers) {
				System.out.println("findMember : " + findMember);
			}
			
			// 트랜잭션 종료
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} finally {
			// EntityManager가 DB connection을 물고 있기 때문에 꼭 닫아줘야 한다.
			em.close();
		}
		
		emf.close();
		
		
	}
	
	
	/**
	 * 
	 * 
	 
	 	프로젝션
		• SELECT 절에 조회할 대상을 지정하는 것
		• 프로젝션 대상: 엔티티, 임베디드 타입, 스칼라 타입(숫자, 문자등 기본 데이터 타입)
		• SELECT m FROM Member m -> 엔티티 프로젝션
		• SELECT m.team FROM Member m -> 엔티티 프로젝션
		• SELECT m.address FROM Member m -> 임베디드 타입 프로젝션
		• SELECT m.username, m.age FROM Member m -> 스칼라 타입 프로젝션
		• DISTINCT로 중복 제거

	 */
	public static void 프로젝션() {
		/**
		 * 애플리케이션 로딩 시점에 한번만 만든다. 데이터베이스당 1개만 실행한다.
		 * persistence.xml의 persistence-unit name 과 맵핑된다.
		 */
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		
		// 트랜젝션이 일어나는 시점에 계속 만들어 준다.
		EntityManager em = emf.createEntityManager();

		// JPA에서는 데이터 변경 관련 모든 작업은 트랙젝션 안에서 실행되어야 한다. 
		EntityTransaction tx = em.getTransaction();
		// 트랜잭션 시작
		tx.begin();
		
		try {
			
			
			Team team = new Team();
			team.setName("teamA");
			em.persist(team);
			
			Member member = new Member();
			member.setName("member1");
			member.setComp("crizen");
			member.setAge(10);
			member.changeTeam(team);
			em.persist(member);
			
			
			em.flush();
			em.clear();

			Member findMember = em.find(Member.class, member.getId());
			System.out.println("findMember team : " + findMember.getTeam().getName());
			
			for (Member m : findMember.getTeam().getMembers()) {
				
				System.out.println("findMember.team.getMembers : " + m.getName());
			}
			
			
			em.flush();
			em.clear();
			
			/**
			 * 
			 * 엔티티 프로젝션 
			 * - 영속성 컨텍스트에 관리가 된다.
			 */
			List<Member> findMembers = em.createQuery("select m from Member m", Member.class).getResultList();
			
			for (Member m : findMembers) {
				System.out.println("find Member : " + m.getName());
			}
			
			
			em.flush();
			em.clear();
			
			/**
			 * 
			 * 엔티티 프로젝션 
			 * - 영속성 컨텍스트에 관리가 된다.
			 * 
			 * 아래와 같이 참조 객체를 조회 하면 조인 쿼리로 조회해 온다.
			 * 실제 사용 시에는 명확하게 조인으로 써야한다.
			 * em.createQuery("select t from Member m join m.team t", Team.class)
			 */
			List<Team> findTeam1 = em.createQuery("select m.team from Member m", Team.class).getResultList();
			
			for (Team t1 : findTeam1) {
				System.out.println("team1 name : " + t1.getName());
			}
			
			em.flush();
			em.clear();
			
			/**
			 * 조인 SQL로 명확하게 사용하기
			 */
			List<Team> findTeam2 = em.createQuery("select t from Member m join m.team t", Team.class).getResultList();
			
			for (Team t2 : findTeam2) {
				System.out.println("team2 name : " + t2.getName());
			}
			
			em.flush();
			em.clear();
			
			
			/**
			 * 임베디드 타입 프로젝션
			 */
			List<Address> findAddresses = em.createQuery("select o.address from Order o", Address.class).getResultList();
			
			for (Address address : findAddresses) {
				System.out.println(" address city : " + address.getCity());
				System.out.println(" address street: " + address.getStreet());
				System.out.println(" address zipcode: " + address.getZipcode());
			}
			
			em.flush();
			em.clear();
			
			/**
			 * 스칼라 타입 프로젝션
			 * 1. Query Type
			 */
			Query query1 = em.createQuery("select m.name, m.age from Member m");
			
			List list = query1.getResultList();
			Object object = list.get(0);
			Object[] objectArr = (Object[]) object;
			for (Object o : objectArr) {
				System.out.println("result : " + o);
			}
			
			em.flush();
			em.clear();
			
			/**
			 * 스칼라 타입 프로젝션
			 * 2. Object[] 타입
			 */
			List resultList = em.createQuery("select m.name, m.age from Member m").getResultList();
			
			Object object1 = resultList.get(0);
			Object[] objectArr1 = (Object[]) object1;
			for (Object o : objectArr1) {
				System.out.println("result1 : " + o);
			}
			
			em.flush();
			em.clear();
			
			/**
			 * 스칼라 타입 프로젝션
			 * 2. Object[] 타입 개선
			 */
			List<Object[]> objectList = em.createQuery("select m.name, m.age from Member m").getResultList();
			
			Object[] objectArr2 = objectList.get(0);
			for (Object o : objectArr2) {
				System.out.println("result2 : " + o);
			}
			
			em.flush();
			em.clear();
			
			/**
			 * 스칼라 타입 프로젝션
			 * 3. new 명령어로 조회
			 */
			List<MemberDTO> memberDTOs = em.createQuery("select new jpql.MemberDTO(m.name, m.age) from Member m", MemberDTO.class).getResultList();
			
			for (MemberDTO mDTO : memberDTOs) {
				System.out.println("MemberDTO name : " + mDTO.getName());
				System.out.println("MemberDTO age : " + mDTO.getAge());
			}
			
			// 트랜잭션 종료
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
			tx.rollback();
		} finally {
			// EntityManager가 DB connection을 물고 있기 때문에 꼭 닫아줘야 한다.
			em.close();
		}
		
		emf.close();
	}
	
	
	/**
	 * 
	 * JPQL - 기본 문법과 기능
	 * 
	 	JPQL 문법
		• select m from Member as m where m.age > 18 
		• 엔티티와 속성은 대소문자를 구분한다. (Member, age)
		• JPQL 키워드는 대소문자를 구분하지 않는다. (SELECT, FROM, where)
		• 테이블 이름이 아닌 엔티티 이름 사용한다. 
			@Entity(name = "Member") : name을 지정하지 않으면 default 는 class 명과 동일하다. 
		• 별칭은 필수(m) (as는 생략가능)


		집합과 정렬
			select
		 		COUNT(m), //회원수
		 		SUM(m.age), //나이 합
		 		AVG(m.age), //평균 나이
		 		MAX(m.age), //최대 나이
		 		MIN(m.age) //최소 나이
			from Member m

		• GROUP BY, HAVING
		• ORDER BY
		
		
		TypeQuery, Query
		• TypeQuery: 반환 타입이 명확할 때 사용
		• Query: 반환 타입이 명확하지 않을 때 사용
		
	 * 
	 */
	public static void jpql_기본문법() {
		/**
		 * 애플리케이션 로딩 시점에 한번만 만든다. 데이터베이스당 1개만 실행한다.
		 * persistence.xml의 persistence-unit name 과 맵핑된다.
		 */
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		
		// 트랜젝션이 일어나는 시점에 계속 만들어 준다.
		EntityManager em = emf.createEntityManager();

		// JPA에서는 데이터 변경 관련 모든 작업은 트랙젝션 안에서 실행되어야 한다. 
		EntityTransaction tx = em.getTransaction();
		// 트랜잭션 시작
		tx.begin();
		
		try {
			
			
			Member member = new Member();
			member.setName("member1");
			member.setComp("crizen");
			member.setAge(10);
			em.persist(member);
			
			
			//------------------------------------------------------------------------------------------------------------------------
			/**
			 * 
			 * TypeQuery: 반환 타입이 명확할 때 사용
			 * 
			 * 1. Member 객체의 모든 컬럼 조회
			 * 
			 */
			TypedQuery<Member> typedQuery = em.createQuery("select m from Member m", Member.class);
			/**
			 * 
			 	• query.getResultList() 
			 		• 결과가 하나 이상일 때, 리스트를  반환한다.
					• 결과가 없으면 빈 리스트를 반환한다.
			 */
			List<Member> findMembers = typedQuery.getResultList();
			
			for (Member m : findMembers) {
				System.out.println("find Member : " + m.getName());
			}
			
			/**
			 * 
				• query.getSingleResult() 
					• 결과가 정확히 하나, 단일 객체 반환
					• 결과가 없으면: javax.persistence.NoResultException
					• 둘 이상이면: javax.persistence.NonUniqueResultException
			 */
			Member singMember = typedQuery.getSingleResult();
			System.out.println("singMember : " + singMember.getName());
			//------------------------------------------------------------------------------------------------------------------------
			
			
			
			//------------------------------------------------------------------------------------------------------------------------
			/**
			 * 
			 * TypeQuery: 반환 타입이 명확할 때 사용
			 * 
			 * 2. Member 객체의 특정 컬럼 조회
			 * 	조회 하는 컬럼의 타입이 일치하는 경우 아래와 같이도 반환타입을 지정할 수 있다.
			 * 	단, String.class 는 하나의 컬럼나 조회 가능하다.
			 */
			TypedQuery<String> typedQuery2 = em.createQuery("select m.name from Member m", String.class);
			
			/**
			 * 
			 	• query.getResultList() 
			 		• 결과가 하나 이상일 때, 리스트를  반환한다.
					• 결과가 없으면 빈 리스트를 반환한다.
				
				• query.getSingleResult() 
					• 결과가 정확히 하나, 단일 객체 반환
					• 결과가 없으면: javax.persistence.NoResultException
					• 둘 이상이면: javax.persistence.NonUniqueResultException
				
			 */
			List<String> names = typedQuery2.getResultList();
			for (String name : names) {
				System.out.println("single name : " + name);
			}
			//------------------------------------------------------------------------------------------------------------------------
			
			
			
			//------------------------------------------------------------------------------------------------------------------------
			/**
			 * TypeQuery: 반환 타입이 명확하지 않을 때
			 * - m.name 은 String 이고, m.age는 int 인 경우 Query 사용해야 한다.
			 */
			Query query = em.createQuery("select m.name, m.age from Member m ");
			
			
			/**
			 * 
			 	• query.getResultList() 
			 		• 결과가 하나 이상일 때, 리스트를  반환한다.
					• 결과가 없으면 빈 리스트를 반환한다.
				
				• query.getSingleResult() 
					• 결과가 정확히 하나, 단일 객체 반환
					• 결과가 없으면: javax.persistence.NoResultException
					• 둘 이상이면: javax.persistence.NonUniqueResultException
				
			 */
			List<?> list = query.getResultList();
			
			for (Object obj : list) {
				Object[] results =  (Object[]) obj;
				for (Object result : results) {
					System.out.println("find result : " + result);
				}
			}
			//------------------------------------------------------------------------------------------------------------------------
			
			
			
			//------------------------------------------------------------------------------------------------------------------------
			/**
			 * 파라미터 바인딩
			 * 
			 */
			
			List<Member> findMember = em.createQuery("select m from Member m where m.name =: name and comp =: comp", Member.class)
														.setParameter("name", "member1")
														.setParameter("comp", "crizen")
														.getResultList();
			
			for (Member m : findMember) {
					System.out.println("fineMembers name : " + m.getName());
			}
			//------------------------------------------------------------------------------------------------------------------------
			
			
			// 트랜잭션 종료
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
			tx.rollback();
		} finally {
			// EntityManager가 DB connection을 물고 있기 때문에 꼭 닫아줘야 한다.
			em.close();
		}
		
		emf.close();
		
	}
	
	
	public static void genericTest() {
		
		Generic<String> gen = new Generic<String>();
		
		gen.add("test1");
		gen.add("test2");
		gen.add("test3");
		gen.add("test4");
		
		for (String  aa : gen.getList()) {
			System.out.println("aa : " + aa);
		}
		
		Generic<Integer> gen1 = new Generic<Integer>();
		
		gen1.add(1);
		gen1.add(2);
		gen1.add(3);
		gen1.add(4);
		gen1.add(5);
		
		for (Integer  aa : gen1.getList()) {
			System.out.println("aa : " + aa);
		}
	}
}
