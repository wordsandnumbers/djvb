package com.vpo.djvoxbox.app;

import java.util.Date;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.vpo.djvoxbox.domain.Manager;
import com.vpo.djvoxbox.domain.ManagerRepository;

@Service
@Component
public class UpdateService {

	@Autowired ManagerRepository managerRepository;
	@Autowired QueueManagementService queueManagementService;
	
	@Value("${manager.name}")
	private String MANAGER_NAME;

	@PostConstruct
	public void ensureManagerExists() {
		if (managerRepository.findByName(MANAGER_NAME) == null) {
			Manager manager = new Manager();
			manager.setName(MANAGER_NAME);
			manager.setActive(true);
			manager.setLastUpdate(new Date(0));
			managerRepository.save(manager);
		}
	}
	
	@Scheduled(fixedDelayString = "${manager.reconcileMs:300000}")
	public void update() {
		/*
		 * look for a manager instance where the manager is active, it's been more than 30 seconds since an update
		 *  */
		Manager manager = managerRepository.returnForWork(MANAGER_NAME);
		if(manager == null) {
			// if there hasn't been 30 seconds since an update look for if there's someone who has been working on it for more than 30 seconds
			manager = managerRepository.returnForUsurp(MANAGER_NAME);
		}
		if (manager != null) {
			try {
				queueManagementService.manageQueues();
			} catch(Exception e) {
				
				System.out.println("Whoops : " + e.toString());
			}
			manager.setLastUpdate(new Date());
			manager.setUsurping(null);
			manager.setWorkLock(null);
		}
		if (manager != null) {
			managerRepository.save(manager);
		}
	}

	public Manager activate() {
		Manager manager = managerRepository.findByName(MANAGER_NAME);
		manager.setActive(true);
		managerRepository.save(manager);
		return manager;
	}
	
	public Manager deactivate() {
		Manager manager = managerRepository.findByName(MANAGER_NAME);
		manager.setActive(false);
		managerRepository.save(manager);
		return manager;
	}
	
	public Manager createManager(String name) {
		Manager manager = new Manager();
		manager.setName(name);
		manager.setActive(false);
		manager.setLastUpdate(new Date());
		managerRepository.save(manager);
		return manager;
	}
	
	public List<Manager> getManagers() {
		return managerRepository.findAll();
	}
	
	public Manager getManager() {
		return managerRepository.findByName(MANAGER_NAME);
	}
	
}
