package com.nisum.carpool.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.nisum.carpool.data.dao.api.CarpooldetailsDAO;
import com.nisum.carpool.data.domain.Carpooldetails;
import com.nisum.carpool.data.util.Pool_Status;
import com.nisum.carpool.service.dto.CarpooldetailsDto;
import com.nisum.carpool.service.dto.ServiceStatusDto;
import com.nisum.carpool.util.CarpooldetailsServiceUtil;

@RunWith(MockitoJUnitRunner.class)
public class CarpooldetailsServiceImplTest {
	
	@InjectMocks
	CarpooldetailsServiceImpl carpooldetailsServiceImpl;
	
	@Mock
	CarpooldetailsDAO carpooldetailsDAO;
	
	//@Mock
	//ObjectUtils objectUtils;
	
	CarpooldetailsDto carpooldetailsDto = new CarpooldetailsDto();
	Pool_Status poolStatus;
	
	//carpooldetails.setStatus(Pool_Status.valueOf(carpooldetailsDto.getStatus().toString()));
	
	@Before
	public void setUp() throws Exception{
		Timestamp createdDate = new Timestamp(1511249628);
		Timestamp modifiedDate = carpooldetailsServiceImpl.currentDate;
		CarpooldetailsDto carpooldetailsDto = new CarpooldetailsDto();
		carpooldetailsDto.setId(1);
		carpooldetailsDto.setParentid(1);
		carpooldetailsDto.setEmailId("mbheemanapalli@nisum.com");

		carpooldetailsDto.setVehicleType(2);
		carpooldetailsDto.setTotalNoOfSeats(1);
		carpooldetailsDto.setFromDate("2017-11-23");
		carpooldetailsDto.setToDate("2017-13-23");
		carpooldetailsDto.setStartTime("12:30");
		carpooldetailsDto.setToTime("13:30");
		carpooldetailsDto.setStatus(1);
		carpooldetailsDto.setCreateddate(createdDate);
		carpooldetailsDto.setModifieddate(modifiedDate);
		
	}

	@Test
	public void updateCarpooldetailsTest() {
		Carpooldetails convertDtoTODao = CarpooldetailsServiceUtil.convertDtoTODao(carpooldetailsDto);
		when(carpooldetailsDAO.updateCarpooldetails(convertDtoTODao))
				.thenReturn("Current record Updated Successfully !!");
		// when(objectUtils.allNotNull(convertDtoTODao)).thenReturn(true);
		ServiceStatusDto serviceStatusDto = new ServiceStatusDto();
		serviceStatusDto.setStatus(true);
		serviceStatusDto.setMessage("Current record Updated Successfully !!");
		ServiceStatusDto actualStatus = carpooldetailsServiceImpl.updateCarpooldetails(carpooldetailsDto);
		assertEquals(serviceStatusDto.getMessage(), actualStatus.getMessage());
	}

	@Test
	public void cancelCarpoolTest() {
		carpooldetailsDto.setEmailId("smamidala@nisum.com");
		carpooldetailsDto.setStatus(4);
		Carpooldetails convertDtoTODao = CarpooldetailsServiceUtil.convertUpdateDtoTODao(carpooldetailsDto);
		when(carpooldetailsDAO.cancelCarpooldetails(convertDtoTODao)).thenReturn("Carpool Cancelled Successfully !!");
		ServiceStatusDto serviceStatusDto = new ServiceStatusDto();
		serviceStatusDto.setStatus(true);
		serviceStatusDto.setMessage("Carpool Cancelled Successfully !!");
		ServiceStatusDto resultStaus=carpooldetailsServiceImpl.cancelCarpooldetails(carpooldetailsDto);
		assertNotNull(resultStaus);
		assertEquals(serviceStatusDto.getMessage(), resultStaus.getMessage());
		
	}
	
	/**
	 * @author Harish Kumar Gudivada
	 * 
	 * Test cases to load the carpoolDetails
	 */
	
	@Test
	public void getCarpoolDetails() {
		try {
			Carpooldetails expectedCarpoolDao = CarpooldetailsServiceUtil.convertDtoTODao(carpooldetailsDto);

			when(carpooldetailsDAO.loadCarpoolDetailsById(1)).thenReturn(expectedCarpoolDao);

			Carpooldetails actualCarpoolDao1 =  CarpooldetailsServiceUtil.convertDtoTODao(carpooldetailsServiceImpl.loadCarpoolDetailsById(1));
			
			assertEquals(expectedCarpoolDao,actualCarpoolDao1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
