package nor.http;


import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ContentTypeTest {

	private ContentType type;

	@Before
	public void setUp() throws Exception {

		this.type = new ContentType("text/plain; charset=utf-8; param1=1; param2=two");

	}

	@After
	public void tearDown() throws Exception {

		this.type = null;

	}

	@Test
	public void testGetType(){

		Assert.assertEquals("text", this.type.getType());

	}

	@Test
	public void testGetSubType(){

		Assert.assertEquals("plain", this.type.getSubtype());

	}

	@Test
	public void testParameterSize(){

		Assert.assertEquals(3, this.type.getParameterSize());

	}

	@Test
	public void testParameterKeys(){

		Assert.assertTrue(this.type.getParameterKeys().contains("param1"));
		Assert.assertFalse(this.type.getParameterKeys().contains("param3"));

	}

	@Test
	public void testContainsParameterKey(){

		Assert.assertTrue(this.type.containsParameterKey("param1"));
		Assert.assertFalse(this.type.containsParameterKey("param3"));

	}

	@Test
	public void testParameterValue(){

		Assert.assertEquals("utf-8", this.type.getParameterValue("charset"));

	}

	@Test
	public void testUnknownParameterValue(){

		Assert.assertEquals(null, this.type.getParameterValue("charset++"));

	}

	@Test(expected = NullPointerException.class)
	public void testNullParameterValue(){

		System.out.println(this.type.getParameterValue(null));

	}

	@Test
	public void testContainsParameterValue(){

		Assert.assertTrue(this.type.containsParameterValue("two"));
		Assert.assertFalse(this.type.containsParameterValue("four"));

	}

	@Test
	public void testGetCharset(){

		Assert.assertEquals("utf-8", this.type.getCharset());

	}

	@Test
	public void testGetNullCharset(){

		final ContentType t2 = new ContentType();
		Assert.assertNull(t2.getCharset());

	}

	@Test
	public void testEquals(){

		final ContentType t2 = new ContentType("text/plain; charset=utf-8; param1=1; param2=two");
		Assert.assertTrue(t2.equals(this.type));


	}

	@Test
	public void testEqualsToNull(){

		Assert.assertFalse(this.type.equals(null));

	}

	@Test
	public void testHashcode(){

		final ContentType t2 = new ContentType("text/plain; charset=utf-8; param1=1; param2=two");
		Assert.assertTrue(t2.hashCode() == this.type.hashCode());

	}

	@Test
	public void testToString(){

		final ContentType t2 = new ContentType(this.type.toString());
		Assert.assertTrue(t2.equals(this.type));

	}

}
