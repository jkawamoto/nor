package nor.http;


import junit.framework.Assert;

import org.junit.Test;

public class StatusTest {

	@Test
	public void testValueOf(){

		Assert.assertEquals(Status.NotFound, Status.valueOf(404));

	}

	@Test(expected = NullPointerException.class)
	public void testValueOfNull(){

		Status.valueOf(null);

	}

	@Test
	public void testValueOfUnknownValue(){

		Assert.assertEquals(Status.NonStandard, Status.valueOf(-1));

	}

	@Test
	public void testValues(){

		for(final Status s : Status.values()){

			if(s == Status.NotFound){

				return;

			}

		}

		Assert.fail();

	}

}
