package jpql;

import java.util.Iterator;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
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
		서브쿼리();
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
