package com.driver.services.impl;

import com.driver.model.Country;
import com.driver.model.CountryName;
import com.driver.model.ServiceProvider;
import com.driver.model.User;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository3;
    @Autowired
    ServiceProviderRepository serviceProviderRepository3;
    @Autowired
    CountryRepository countryRepository3;

    @Override
    public User register(String username, String password, String countryName) throws Exception{
        CountryName countryName1 = CountryName.valueOf(countryName);

        Country country = new Country();
        country.setCountryName(countryName1);
        country.setCode(countryName1.toCode());
        country.setServiceProvider(null);

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setCountry(country);
        user.setConnected(false);
        user.setMaskedIp(null);
        user = userRepository3.save(user);

        user.setOriginalIp(country.getCode() + "." + user.getId());

        return userRepository3.save(user);
    }

    @Override
    public User subscribe(Integer userId, Integer serviceProviderId) {
        ServiceProvider serviceProvider = serviceProviderRepository3.findById(serviceProviderId).get();

        User user = userRepository3.findById(userId).get();
        user.getServiceProviderList().add(serviceProvider);

        serviceProvider.getUsers().add(user);

        return userRepository3.save(user);
    }
}
