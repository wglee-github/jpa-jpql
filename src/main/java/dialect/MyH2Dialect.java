package dialect;

import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

/**
 * 
 * @author wglee
 *
 *	사용자 정의 함수 
 *
 */
public class MyH2Dialect extends H2Dialect{

	/**
	 * registerFunction() 사용자 정의 함수는 hibernate 6.x 부터 사용할 수 없는 듯.
	 * 내부적으로 변경이 꽤 있는 듯. 일단 PASS
	 * 
	 * 만약 사용자 정의 함수를 사용하기 위해 해당 class를 사용하는 경우 
	 * persistence.xml 설정 중 hibernate.dialect 프로퍼티 value를 해당 클랙스 패키지로 변경해줘야 함.
	 * 기존 : <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect" />
	 * 변경 : <property name="hibernate.dialect" value="dialect.MyH2Dialect" />
	 * 
	 */
	public MyH2Dialect() {
//		registerFunction("group_concat", new StandardSQLFunction("group_concat",StandardBasicTypes.STRING));
	}

	
	
}
