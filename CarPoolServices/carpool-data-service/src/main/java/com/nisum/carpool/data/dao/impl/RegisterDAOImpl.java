package com.nisum.carpool.data.dao.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.nisum.carpool.data.dao.api.RegisterDAO;
import com.nisum.carpool.data.domain.RegisterDomain;
import com.nisum.carpool.data.repository.RegisterRepository;

@Configuration
public class RegisterDAOImpl implements RegisterDAO {

	private static Logger logger = LoggerFactory.getLogger(RegisterDAOImpl.class);
	@Autowired
	RegisterRepository registerRepository;
	@Override
	public RegisterDomain registerDriverorRider(RegisterDomain registerDomain)    {
		try{
			logger.info("RegisterDAOImpl: registerDriver ::");
			registerRepository.save(registerDomain);
		}catch (Exception e) {
			e.printStackTrace();
		}
		 return registerDomain;
	}
	
	/**
	 * @author Harish Kumar Gudivada
	 * Param emailId
	 * Return registerList
	 * This method is used to load the rider or driver or both details by emailId
	 */
	@Override
	public List<RegisterDomain> findUserRegistrationByUserId(String emailId) {
		logger.info("UserRegistrationDaoImpl :: findUserRegistrationByUserId :: Finding user by emailId");
		List<RegisterDomain> registerList=null;
		try {
			registerList=registerRepository.findByEmailId(emailId);
		}catch (Exception e) {
			logger.error("Exception Occured in Class:UserRegistrationDaoImpl Method:findUserRegistrationByUserId Message:"+e.getMessage());
		}
		logger.info("Exit from UserRegistrationDaoImpl :: findUserRegistrationByUserId");
		return registerList;
	}
	
	/**
	 * @author Harish Kumar Gudivada
	 * Param emailId
	 * Return location
	 * This method is used to load the Location Of Registered User With emailId
	 */
	@Override
	public String getLocationOfRegisteredUser(String emailId) {
		logger.info("UserRegistrationDaoImpl :: getLocationOfRegisteredUser :: Finding Location by emailId");
		String location="";
		try {
		List<RegisterDomain> list=registerRepository.findByEmailId(emailId);
		for(RegisterDomain registedDao:list) {
			if(registedDao.getIsrider()==0)
				location=registedDao.getLocation();
		}
		}catch (Exception e) {
			logger.error("Exception Occured in Class:UserRegistrationDaoImpl Method :getLocationOfRegisteredUser Message:"+e.getMessage());
		}
		logger.info("Exit from UserRegistrationDaoImpl:: getLocationOfRegisteredUser");
		return location;
	}

	


}
