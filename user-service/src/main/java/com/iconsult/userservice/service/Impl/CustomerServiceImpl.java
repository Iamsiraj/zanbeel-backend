package com.iconsult.userservice.service.Impl;

import com.iconsult.userservice.exception.ServiceException;
import com.iconsult.userservice.model.dto.request.CustomerDto;
import com.iconsult.userservice.model.dto.request.ForgetUsernameDto;
import com.iconsult.userservice.model.dto.request.LoginDto;
import com.iconsult.userservice.model.dto.response.ResponseDTO;
import com.iconsult.userservice.model.entity.Customer;
import com.iconsult.userservice.model.mapper.CustomerMapperImpl;
import com.iconsult.userservice.repository.CustomerRepository;
import com.iconsult.userservice.service.CustomerService;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.zanbeel.customUtility.model.CustomResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import javax.ws.rs.core.MediaType;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final String URL = "http://iconsult-21:8081/account/verifyAccount?cnic=%s&accountNumber=%s";
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    private CustomResponseEntity<ResponseDTO> response;
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerMapperImpl customerMapperImpl;

    @Override
    public CustomResponseEntity<ResponseDTO> register(CustomerDto customerDto)
    {
        LOGGER.info("Sign up Request received");

        if(!accountExist(customerDto))
        {
            LOGGER.error("Customer account does not exist [" + customerDto.getAccountNumber() + "], cannot allow signup, rejecting...");
            throw new ServiceException(String.format("Customer account does not exist [%s]", customerDto.getAccountNumber()));
        }

        // Duplicate Customer Check
        Customer customerDuplicate = findByMobileNumber(customerDto.getMobileNumber());

        if(customerDuplicate != null)
        {
            LOGGER.error("Customer already exists with mobile [" + customerDto.getMobileNumber() + "], cannot allow signup, rejecting...");
            throw new ServiceException(String.format("Customer with Mobile Number %s already exists", customerDto.getMobileNumber()));
        }

        // Duplicate Customer Check
        customerDuplicate = findByEmailAddress(customerDto.getEmail());

        if(customerDuplicate != null)
        {
            LOGGER.error("Customer already exists with Email [" + customerDto.getEmail() + "], cannot allow signup, rejecting...");
            throw new ServiceException(String.format("Customer with Email %s already exists", customerDto.getEmail()));
        }

        Customer customer = addUser(customerMapperImpl.dtoToJpe(customerDto));

        LOGGER.info("Customer has been saved with Id {}", customer.getId());

        response = new CustomResponseEntity<>(new ResponseDTO("Success", 200), null);
        response.getData().addField("mobileNumber", customer.getMobileNumber());

        return new ResponseEntity<>(response, HttpStatus.CREATED).getBody();
    }

    @Override
    public CustomResponseEntity<ResponseDTO> login(LoginDto loginDto)
    {
        Customer customer = customerRepository.findByEmail(loginDto.getEmail());
        if(customer != null)
        {
            if(customer.getEmail().equals(loginDto.getEmail()) && customer.getPassword().equals(loginDto.getPassword()))
            {
                response = new CustomResponseEntity<>(new ResponseDTO("Success", 200), null);
                return response;
            }
            else
            {
                throw new ServiceException("Invalid Email or Password");
            }
        }

        throw new ServiceException("Customer does not exist");
    }

    @Override
    public Customer addUser(Customer customer)
    {
        return save(customer);
    }

    @Override
    public void deleteUser(Long id) {
        this.customerRepository.deleteById(id);
    }

    @Override
    public Customer updateCustomer(Customer customer) {
        return this.customerRepository.save(customer);
    }

    @Override
    public Customer save(Customer customer)
    {
        return customerRepository.save(customer);
    }

    @Override
    public CustomResponseEntity<Customer> findById(Long id) {
        Optional<Customer> student = this.customerRepository.findById(id);
        if (!student.isPresent()) {
            throw new ServiceException("Customer Not Found");
        }
        return CustomResponseEntity.<Customer>builder().data(student.get()).build();
    }

    @Override
    public CustomResponseEntity<ResponseDTO> forgetUserName(ForgetUsernameDto forgetUsernameDto)
    {
        LOGGER.info("ForgetUsername Request Received...");

        Customer customer = findByEmailAddress(forgetUsernameDto.getEmail());

        if(customer != null)
        {
            LOGGER.info("Customer found with Email Address [{}], sending username on email...", forgetUsernameDto.getEmail());
            // Kafka email send here
            forgetUsernameDto.setUserName(customer.getUserName());
            this.kafkaTemplate.send("forgetUserName", forgetUsernameDto);

            LOGGER.info("Email sent [{}]", forgetUsernameDto.getEmail());
            response = new CustomResponseEntity<>(new ResponseDTO("Success", 200), null);
            return response;
        }

        LOGGER.info("Customer Email does not exists, verifying mobile number...");

        customer = findByMobileNumber(forgetUsernameDto.getMobileNumber());

        if(customer != null)
        {
            LOGGER.info("Customer found with Mobile Number [{}], sending username on email...", forgetUsernameDto.getMobileNumber());
            // Kafka SMS send here
            forgetUsernameDto.setUserName(customer.getUserName());
            this.kafkaTemplate.send("forgetUserName", forgetUsernameDto);

            LOGGER.info("SMS sent [{}]", forgetUsernameDto.getMobileNumber());
            response = new CustomResponseEntity<>(new ResponseDTO("Success", 200), null);
            return response;
        }

        throw new ServiceException("User Not Found");
    }

    @Override
    public Customer findByEmailAddress(String email)
    {
        return this.customerRepository.findByEmail(email);
    }

    @Override
    public Customer findByMobileNumber(String mobileNumber) {
        return this.customerRepository.findByMobileNumber(mobileNumber);
    }

    public List<Customer> findAllUsers()
    {
        return this.customerRepository.findAll();
    }

    public Customer updateCustomer(CustomerDto customerDto)
    {

        Customer updateCustomer = this.customerRepository.findByEmail(customerDto.getEmail());

        if(updateCustomer == null)
        {
            throw new ServiceException(String.format("User with email %s not found", customerDto.getEmail()));
        }

        updateCustomer.setFirstName(customerDto.getFirstName());
        updateCustomer.setLastName(customerDto.getLastName());
        updateCustomer.setEmail(customerDto.getEmail());

        save(updateCustomer);
        LOGGER.info("Customer has been updated with Id {}", updateCustomer.getId());

        return ResponseEntity.ok(updateCustomer).getBody();
    }

    private Boolean accountExist(CustomerDto customerDto)
    {
        Client client = Client.create();
        client.setConnectTimeout(5 * 1000);
        client.setReadTimeout(5 * 1000);
        WebResource webResource = null;

        try
        {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = (hostname, session) -> true;

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            String url = String.format(URL, customerDto.getCnic(), customerDto.getAccountNumber());
            LOGGER.info("Calling URL [" + url + "]");
            webResource = client.resource(url);

            ClientResponse clientResponse = webResource.type(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .get(ClientResponse.class);

            if(clientResponse != null)
            {
                if(clientResponse.getStatus() != 200)
                {
                    LOGGER.error("Account Service down, rejecting!!");
                    return false;
                }

                String response = clientResponse.getEntity(String.class);

                if(response != null)
                {
                    LOGGER.info("Account Service response found For Customer [{}], proceeding...", customerDto.getMobileNumber());
                    return Boolean.parseBoolean(response);
                }
            }
            else
            {
                LOGGER.error("Failed to Get response from Account Service For Customer [{}], rejecting...", customerDto.getMobileNumber());
            }
            return false;
        }
        catch (Exception e)
        {
            LOGGER.error("Failed to Get response from Account Service For Customer [{}], rejecting...", customerDto.getMobileNumber());
            return false;
        }
    }
}