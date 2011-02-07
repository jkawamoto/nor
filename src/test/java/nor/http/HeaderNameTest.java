package nor.http;


import junit.framework.Assert;

import org.junit.Test;

public class HeaderNameTest {

	@Test
	public void testEquals(){

		Assert.assertTrue(HeaderName.ContentLength.equals("content-length"));
		Assert.assertTrue(HeaderName.ContentLength.equals("Content-Length"));

	}

	@Test
	public void testNotEquals(){

		Assert.assertFalse(HeaderName.ContentLength.equals("Content Length"));

	}

	@Test
	public void testEqualsToNull(){

		Assert.assertFalse(HeaderName.ContentLength.equals(null));

	}

	@Test
	public void testValueOf(){

		Assert.assertEquals(HeaderName.ContentLength, HeaderName.valueOf("Content-Length"));
		Assert.assertEquals(HeaderName.ContentLength, HeaderName.valueOf("content-length"));

	}

	@Test(expected = NullPointerException.class)
	public void testValueOfNull(){

		HeaderName.valueOf(null);

	}

	@Test(expected = IllegalArgumentException.class)
	public void testValueOfWS(){

		HeaderName.valueOf("");

	}

	@Test(expected = IllegalArgumentException.class)
	public void testValueOfUnknownValue(){

		HeaderName.valueOf("Content Length");

	}

	@Test
	public void testValues(){

		for(final HeaderName h : HeaderName.values()){

			if(h == HeaderName.ContentLength){

				return;

			}

		}

		Assert.fail();

	}

}
