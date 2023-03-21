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
		jpql_기본문법();
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
