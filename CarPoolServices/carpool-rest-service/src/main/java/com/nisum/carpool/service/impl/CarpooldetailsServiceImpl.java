package com.nisum.carpool.service.impl;


import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nisum.carpool.data.dao.api.CarpoolRiderDetailsDAO;
import com.nisum.carpool.data.dao.api.CarpooldetailsDAO;
import com.nisum.carpool.data.dao.api.RegisterDAO;
import com.nisum.carpool.data.dao.api.UserDAO;
import com.nisum.carpool.data.domain.CarpoolRiderDetails;
import com.nisum.carpool.data.domain.Carpooldetails;
import com.nisum.carpool.data.domain.RegisterDomain;
import com.nisum.carpool.data.domain.User;
import com.nisum.carpool.data.util.Pool_Status;
import com.nisum.carpool.service.api.CarpooldetailsService;
import com.nisum.carpool.service.dto.CarpooldetailsDto;
import com.nisum.carpool.service.dto.CustomerCarpooldetailsDto;
import com.nisum.carpool.service.dto.DriverCarPoolDto;
import com.nisum.carpool.service.dto.ParentCarpoolDetailsDto;
import com.nisum.carpool.service.dto.ServiceStatusDto;
import com.nisum.carpool.service.exception.CarpooldetailsServiceException;
import com.nisum.carpool.util.CarpooldetailsServiceUtil;
import com.nisum.carpool.util.Constants;

@Service
public class CarpooldetailsServiceImpl implements CarpooldetailsService{

	private static Logger logger = LoggerFactory.getLogger(CarpooldetailsServiceImpl.class);
	@Autowired
	CarpooldetailsDAO carpooldetailsDAO;
	
	@Autowired
	UserDAO userDAO;
	@Autowired
	RegisterDAO registerDAO;
	
	@Autowired
	CarpoolRiderDetailsDAO carpoolRiderDAO;
	
	Timestamp modifiedDate = new Timestamp(System.currentTimeMillis());
	@Override
	public ServiceStatusDto updateCarpooldetails(CarpooldetailsDto carpooldetailsDto) {
		// TODO Auto-generated method stub
		logger.info("CarpooldetailsServiceImpl : updateCarpooldetails");
		carpooldetailsDto.setModifieddate(modifiedDate);
		Carpooldetails carpooldetails = CarpooldetailsServiceUtil.convertDtoTODao(carpooldetailsDto);
		String updateCarpooldetails = carpooldetailsDAO.updateCarpooldetails(carpooldetails);
		ServiceStatusDto serviceStatusDto = new ServiceStatusDto();
		if(ObjectUtils.anyNotNull(updateCarpooldetails))
		{
			logger.info("CarpooldetailsServiceImpl : updateCarpooldetails ::"+updateCarpooldetails);
			serviceStatusDto.setStatus(true);
			serviceStatusDto.setMessage(updateCarpooldetails);
		}
		return serviceStatusDto;
	}
	
	@Override
	public ServiceStatusDto cancelCarpooldetails(CarpooldetailsDto carpooldetailsDto) {
		// TODO Auto-generated method stub
		logger.info("CarpooldetailsServiceImpl : cancel Carpooldetails");
		
		carpooldetailsDto.setModifieddate(modifiedDate);
		
		String cancelCarpooldetails=null;
		try {
			Carpooldetails carpooldetails = CarpooldetailsServiceUtil.convertUpdateDtoTODao(carpooldetailsDto);
			cancelCarpooldetails = carpooldetailsDAO.cancelCarpooldetails(carpooldetails);
			logger.info("Carpooldetails after cancel  dao message::"+cancelCarpooldetails);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ServiceStatusDto serviceStatusDto = new ServiceStatusDto();
		if(ObjectUtils.anyNotNull(cancelCarpooldetails))
		{
			logger.info("CarpooldetailsServiceImpl : cancel Carpooldetails set message::"+cancelCarpooldetails);
			serviceStatusDto.setStatus(true);
			serviceStatusDto.setMessage(cancelCarpooldetails);
		}
		
		return serviceStatusDto;
	}
	
	
	@Override
    public List<CarpooldetailsDto> createCarPooldetails(CarpooldetailsDto carpooldetailsDto) {
        // TODO Auto-generated method stub
        logger.info("CarpooldetailsServiceImpl:createCarPool");
        
        carpooldetailsDto.setCreateddate(new Timestamp(System.currentTimeMillis()));
        carpooldetailsDto.setModifieddate(new Timestamp(System.currentTimeMillis()));

        // code added by Harish Kumar Gudivada on 30th November 2017
        //for loading the location from user registration and saving in the carpool details
        String location = registerDAO.getLocationOfRegisteredUser(carpooldetailsDto.getEmailId());
        carpooldetailsDto.setLocation(location);
        //end
        
       Carpooldetails carpooldetails = CarpooldetailsServiceUtil.convertDtoTODao(carpooldetailsDto);
        
       String validstatus = checkValidCarpool(carpooldetails);
        logger.info("validstatus " + validstatus);
       if(validstatus.equals(Constants.CARPOOLEXISTS)) {
            
           logger.info("PostRideServiceImpl: posting a ride failed ");
            return null;
       }
        
       logger.info("valid code");
        List<Carpooldetails> carPoolList = processPostRideDomain(carpooldetails);
       List<Carpooldetails> cpd = carpooldetailsDAO.addCarpoolDetails(carPoolList);
        

       if(cpd == null) return null; 
       
       else 
        
       return CarpooldetailsServiceUtil.convertDaoTODto(cpd);
      
    }
	public String checkValidCarpool(Carpooldetails carpooldetails) {
		
		//to check if the carpool is already available in db for the user with the given fromdate and todate
		
		logger.info("CarpooldetailsServiceImpl:checkValidCarpool");
		
		return carpooldetailsDAO.checkValidCarpool(carpooldetails);
		
		
	}
	
	public static List<Carpooldetails> processPostRideDomain(Carpooldetails carpooldetails) {
		
		// to create a list of carpool domain objects to be sent to data access layer
		
		List<Carpooldetails> carPoolList = new ArrayList<Carpooldetails>();
	    
	    int parentid = 0;
	    
	    boolean start = true;
	    boolean pdone = false;
	    
	    logger.info("from date " + carpooldetails.getFromDate());
	    logger.info("to date " + carpooldetails.getToDate());
	    
	    int days = CarpooldetailsServiceUtil.getNo_of_days(carpooldetails.getFromDate(), carpooldetails.getToDate());
	    days = days + 1;
	    
	    logger.info("Number of days " + days);
	    for(int i=0;i<days;i++) {
	    	
	    	//checking if parent record is added to list, if yes then start again to add child records
	    	
	    	if(pdone == true) {
	    		i = 0;
	    		pdone = false;
	    	}
	    	
	    	Carpooldetails cp = new Carpooldetails();
	    	int id = CarpooldetailsServiceUtil.getRandomInt();
	    	
	    	//checking if parentid is set, else set parentid to id
	    	
	    	
	    	if(parentid==0)
	    	parentid = id;
	    	cp.setId(id);
	    	cp.setParentid(parentid);
	    	
	    	//setting original fromdate and todate for parent record in carpool db
	    	
	    	if(start == true) {
	    		
	    		cp.setFromDate(carpooldetails.getFromDate());
	    	    cp.setToDate(carpooldetails.getToDate());
	    		start = false;
	    		pdone = true;
	    			
	    	}
	    	
	    	//setting individual fromdate and todate for child records in carpool db
	    	
	    	else {
	    	
	    	cp.setFromDate(CarpooldetailsServiceUtil.getAddedDate(carpooldetails.getFromDate(), i));
	    cp.setToDate(CarpooldetailsServiceUtil.getAddedDate(carpooldetails.getFromDate(), i));
	    
	    	} 
	    	
	    	cp.setFromtime(carpooldetails.getFromtime());
	    	cp.setToTime(carpooldetails.getToTime());
	    	cp.setCreateddate(carpooldetails.getCreateddate());
	    	cp.setModifieddate(carpooldetails.getModifieddate());
	    	cp.setNoofseats(carpooldetails.getNoofseats());

	    //cp.setStatus(Pool_Status.OPEN);

	    	cp.setEmailId(carpooldetails.getEmailId());

	    	cp.setVehicleType(carpooldetails.getVehicleType());
	    	cp.setLocation(carpooldetails.getLocation());
	    	carPoolList.add(cp);
	}
	    
	    return carPoolList;
	    
	}
	/*public List<CustomerCarpooldetailsDto> getCarPoolDetails(String location)
	{
		List<Carpooldetails> carpoolList = new ArrayList<>();
		
		List<CustomerCarpooldetailsDto> carpoolListbyLocation = new ArrayList<>();
		List<CustomerCarpooldetailsDto> customerCarpooldetailsDtoList = new ArrayList<>();

	

	carpoolList = (List<Carpooldetails>) carpooldetailsDAO.getAllCarPoolDetails();
	
		
		if (carpoolList != null) {
			//List<User> usersList = userDAO.getUsers();

			//for (User user : usersList) {
				for (Carpooldetails carpool : carpoolList) {
					
					//if (user.getEmailId().equals(carpool.getUserid())) {
					User user=userDAO.findByEmailId(carpool.getUserid());
					List<RegisterDomain> registerDomain=	registerDAO.findUserRegistrationByUserId(carpool.getUserid());
				
						CustomerCarpooldetailsDto customerCarpooldetailsDto = new CustomerCarpooldetailsDto();
						customerCarpooldetailsDto.setId(carpool.getId());
						// customerCarpooldetailsDto.setCreateddate(carpool.getCreateddate());
						//customerCarpooldetailsDto.getFromDate(carpool.getFromDate());
						
						customerCarpooldetailsDto.setParentid(carpool.getParentid());
						customerCarpooldetailsDto.setTotalNoOfSeats(carpool.getNoofseats());
						customerCarpooldetailsDto.setStatus(carpool.getStatus());
						customerCarpooldetailsDto.setStartTime(carpool.getToDate());
						customerCarpooldetailsDto.setFromDate(carpool.getFromDate());
						customerCarpooldetailsDto.setToTime(carpool.getToTime());
						customerCarpooldetailsDto.setVehicleType(carpool.getVehicleType());
						customerCarpooldetailsDto.setUserid(carpool.getUserid());
						if(user!=null)
						customerCarpooldetailsDto.setUserName(user.getUserName());
if(registerDomain!=null && registerDomain.size()>0) {
						customerCarpooldetailsDto.setLocation(registerDomain.get(0).getLocation());
                           if(registerDomain.get(0).getMobile()!=null)
						customerCarpooldetailsDto.setMobile(registerDomain.get(0).getMobile());
}
						customerCarpooldetailsDtoList.add(customerCarpooldetailsDto);

					}
				}

			
	if(location!=null)
	{
		for(CustomerCarpooldetailsDto customerCarpooldetailsDto:customerCarpooldetailsDtoList)
		{
			if(location.equalsIgnoreCase(customerCarpooldetailsDto.getLocation()))
			{
				carpoolListbyLocation.add(customerCarpooldetailsDto);
			}
		}
		return carpoolListbyLocation;
	}

		return customerCarpooldetailsDtoList;

	}*/
	
	public List<CustomerCarpooldetailsDto> getCarPoolDetails(String location)
	{
		List<Carpooldetails> carpoolLists = new ArrayList<>();
		
		List<CustomerCarpooldetailsDto> carpoolListbyLocation = new ArrayList<>();
		List<CustomerCarpooldetailsDto> customerCarpooldetailsDtoList = new ArrayList<>();
		
		//List<String> useridsList=null;
         Set<String> useridsSet=null;
		//useridsList=	carpooldetailsDAO.getAllUserIDs();
         carpoolLists= (List<Carpooldetails>) carpooldetailsDAO.getAllCarPoolDetails();
         
		if(carpoolLists!=null)
		{
			useridsSet=new HashSet<>();
		for(Carpooldetails car:carpoolLists)
		{
	
			
			useridsSet.add(car.getEmailId());
		}
			
			for(String userid:useridsSet)
			{
				List<Carpooldetails> carpoolList=	carpooldetailsDAO.getCarPoolByMailID(userid);
				User user=userDAO.findByEmailId(userid);
				List<RegisterDomain> registerDomain=	registerDAO.findUserRegistrationByUserId(userid);
				CustomerCarpooldetailsDto carpooldetailsDto=new CustomerCarpooldetailsDto();
				carpooldetailsDto.setListCarpoolDetails(carpoolList);
				if(user!=null)
				carpooldetailsDto.setUserName(user.getUserName());
				if(registerDomain!=null && registerDomain.size()>0) {
				carpooldetailsDto.setLocation(registerDomain.get(0).getLocation());
				carpooldetailsDto.setMobile(registerDomain.get(0).getMobile());
				}
				customerCarpooldetailsDtoList.add(carpooldetailsDto);
				
			
			}
			if(location!=null)
			{
				for(CustomerCarpooldetailsDto customerCarpooldetailsDto:customerCarpooldetailsDtoList)
				{
					if(location.equalsIgnoreCase(customerCarpooldetailsDto.getLocation()))
							{
						carpoolListbyLocation.add(customerCarpooldetailsDto);
							}
				}
				return carpoolListbyLocation;
				
			}
			return customerCarpooldetailsDtoList;

		}

	
	
		return customerCarpooldetailsDtoList;
	}

	
	/**
	 * @author Harish Kumar Gudivada
	 * 
	 * Param carpoolId
	 * Return CarpooldetailsDto
	 * This method is to get the carpool ride details from CarpoolDetailsDAO based on carpool id and convert the Carpooldetails class CarpooldetailsDto
	 */
	@Override
	public CarpooldetailsDto loadCarpoolDetailsById(int carpoolId) {
		logger.error("Entered into CarpooldetailsServiceImpl :: loadCarpoolDetailsById"); 
		CarpooldetailsDto carpoolDetsDto=null;
		try {
			Carpooldetails carpooldets= carpooldetailsDAO.loadCarpoolDetailsById(carpoolId);
			carpoolDetsDto= CarpooldetailsServiceUtil.convertDaoToDtoInstance(carpooldets);
		}catch (Exception e) {
			logger.error("Exception Occured in Class:CarpooldetailsServiceImpl Method:loadCarpoolDetailsById Message:"+e.getMessage());
		}
		logger.error("Exit from CarpooldetailsServiceImpl :: loadCarpoolDetailsById");
		return carpoolDetsDto;
	}
	
	
	public List<ParentCarpoolDetailsDto> getCarpoolsByDriver(String email) throws CarpooldetailsServiceException {
		logger.debug("BEGIN:getCarpoolsByDriver in the" + this.getClass().getName());
		List<Carpooldetails> carpoolsList = null;
		List<CarpoolRiderDetails> ridersList = null;
		List<DriverCarPoolDto> driverCarpoolList = new ArrayList<>();
		List<ParentCarpoolDetailsDto> ParentCarpoolDetailsDtosList=new ArrayList<>();
		String location = null;
		List<Integer> parentIdsList=new ArrayList();
		
		try {
			//carpoolsList = carpooldetailsDAO.getCarpoolsByDriver(email);
			parentIdsList=carpooldetailsDAO.getCarPoolParentIds(email);
			Set<Integer> parentIdsSet=new HashSet<>(parentIdsList);
			
			
			
			String parentStatus=null;
			for(int p:parentIdsSet)
			{
				//getting CarpoolDetais By ParentID
		List<Carpooldetails>	carpoolsByParentId=carpooldetailsDAO.getCarpoolsByParentId(p);
		
		if(carpoolsByParentId!=null)
		{
			ParentCarpoolDetailsDto parentCarpoolDetailsDto=new ParentCarpoolDetailsDto();
			
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			String currentdate=sdf.format(new Date());
			
			for (Carpooldetails carpooldetails : carpoolsByParentId) {
				
				
				Date fromDate = sdf.parse(carpooldetails.getFromDate());
		        Date currentDtae= sdf.parse(currentdate);
				if( carpooldetails.getStatus()==3 || !fromDate.after(currentDtae))
				{
					continue;
				}
				if(carpooldetails.getId().equals(carpooldetails.getParentid()))
				{
					parentCarpoolDetailsDto.setFromDate(carpooldetails.getFromDate());
					parentCarpoolDetailsDto.setToDate(carpooldetails.getToDate());
					parentCarpoolDetailsDto.setLocation(carpooldetails.getLocation());
					parentCarpoolDetailsDto.setParentId(carpooldetails.getParentid());
					
					//ParentCarpoolDetailsDtosList.add(parentCarpoolDetailsDto);
					if(carpoolsByParentId.size()==1)
					{
						ridersList = carpoolRiderDAO.getRidersByCpID(carpooldetails.getId());
						String Riderstatus=getRidersStatus(ridersList,carpooldetails);
						parentCarpoolDetailsDto.setStatus(Riderstatus);
						ParentCarpoolDetailsDtosList.add(parentCarpoolDetailsDto);
						return ParentCarpoolDetailsDtosList;
					}
					continue;
				}
				DriverCarPoolDto driverCarPoolDto = new DriverCarPoolDto();
				
				driverCarPoolDto.setStatus("Open");
				
				ridersList = carpoolRiderDAO.getRidersByCpID(carpooldetails.getId());
				
				String status=null;
				status=getRidersStatus(ridersList,carpooldetails);
					driverCarPoolDto.setStatus(status);
					//driverCarPoolDto.setFromDate(carpooldetails.getFromDate());
					driverCarpoolList.add(driverCarPoolDto);
				}
				
			String parentSta=carPoolStatus(driverCarpoolList,carpoolsByParentId.size()-1);
			parentCarpoolDetailsDto.setStatus(parentSta);
			ParentCarpoolDetailsDtosList.add(parentCarpoolDetailsDto);
			}

		
		}
		} catch (Exception ex) {
			logger.error("Some thing went wrong while fetching getCarpoolsByDriver:: " + ex.getMessage());
			throw new CarpooldetailsServiceException(ex.getMessage());
		}
		logger.debug("END:BEGIN:getCarpoolsByDriver in the " + this.getClass().getName());
		return ParentCarpoolDetailsDtosList;
	}
	
	 public  List<DriverCarPoolDto> getCarPoolsByParentId(int parentId) throws CarpooldetailsServiceException
	{
		 logger.debug("BEGIN: getCarPoolsByParentId in the class"+this.getClass().getName());
		 List<DriverCarPoolDto> driverCarPoolDtoList = new ArrayList();
		 try
		 {
		List<Carpooldetails> carpools = carpooldetailsDAO.getCarpoolsByParentId(parentId);

		
		List<CarpoolRiderDetails> ridersList = null;

		// driverCarPoolDto.setLocation(location);

		for (Carpooldetails carpooldetails : carpools) {
			DriverCarPoolDto driverCarPoolDto = new DriverCarPoolDto();
			ridersList = carpoolRiderDAO.getRidersByCpID(carpooldetails.getId());
			driverCarPoolDto.setFromDate(carpooldetails.getFromDate());
			driverCarPoolDto.setToDate(carpooldetails.getToDate());
			driverCarPoolDto.setLocation(carpooldetails.getLocation());
			String status = getRidersStatus(ridersList, carpooldetails);
			driverCarPoolDto.setStatus(status);
			driverCarPoolDtoList.add(driverCarPoolDto);
		}
		logger.debug("END: getCarPoolsByParentId in the class"+this.getClass().getName());
		 }
		 catch(Exception ex)
		 {
			 logger.error("Some thing went wrong while fetching getCarpoolsByDriver::");
				throw new CarpooldetailsServiceException(ex.getMessage());
			 
			 
		 }
		return driverCarPoolDtoList;
	}
	
	private String getRidersStatus(List<CarpoolRiderDetails> ridersList,Carpooldetails carpooldetails)
	{
		if (ridersList != null && ridersList.size() > 0) {
			int requestedCount = 0;
			int approvedCount = 0;
			int rejectedCount = 0;
             
			for (CarpoolRiderDetails rider : ridersList) {

				switch (rider.getStatus()) {
				case 1:// requested
					++requestedCount;
					break;
				case 2:// approved
					++approvedCount;
					break;
				case 3:// rejected
					++rejectedCount;
				case 4:// canceled
					++rejectedCount;
					break;

				}
			}
			String status =null;
			if (approvedCount == carpooldetails.getNoofseats()) {
				status = "Closed";
			} else if ((approvedCount + requestedCount) == carpooldetails.getNoofseats()) {
				status = "Completed";
			} else if ((approvedCount + requestedCount) > 0
					&& (approvedCount + requestedCount) < carpooldetails.getNoofseats()) {
				status = "Partially Completed";
			} 
			else
			{
				status="Open";
			}
			return status;
	}
		return "Open";

}
	private String carPoolStatus(List<DriverCarPoolDto> driverCarpoolList,int poolCount)
	{
		int closedCount=0;
		int openCount=0;
		int completedCount=0;
		int partiallyCompletedCount=0;
		for(DriverCarPoolDto c:driverCarpoolList)
		{
			switch(c.getStatus().trim().toLowerCase())
			{
			case "closed":
				++closedCount;
				break;
			
			case "partiallycompleted":
				++partiallyCompletedCount;
			        break;
			case "completed":
				++completedCount;
				
			}
			if(closedCount>=poolCount)
			{
				return "Closed";
			}
			else 	if((closedCount+completedCount)==poolCount)
			{
				return "Completed";
			}
			else 	if((closedCount+completedCount)>0 && (closedCount+completedCount)<poolCount )
			{
				return "Partially Completed";
			}
			else
			{
				return "Open";
			}
			
		
	}
		return "Open";
	}
	
	
}
