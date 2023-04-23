package com.revature.EmployeeManagement.Service;

import com.revature.EmployeeManagement.Exception.InvalidCredential;
import com.revature.EmployeeManagement.Model.Employee;
import com.revature.EmployeeManagement.Model.Goal;
import com.revature.EmployeeManagement.Model.Notification;
import com.revature.EmployeeManagement.Repositoty.EmployeeRepository;
import com.revature.EmployeeManagement.Repositoty.GoalRepository;
import com.revature.EmployeeManagement.Repositoty.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.List;
import java.util.Optional;

@Service
public class GoalService {

    private GoalRepository goalRepository;

    private EmployeeRepository employeeRepository;
    private NotificationService notificationService;
    private NotificationRepository notificationRepository;
    @Autowired
    public GoalService(GoalRepository goalRepository, EmployeeRepository employeeRepository,
                       NotificationRepository notificationRepository, NotificationService notificationService){
        this.goalRepository = goalRepository;
        this.employeeRepository = employeeRepository;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Assign a new goal to an employee
     * @param goal
     * @param employeeId
     * @return
     */

    public Goal createGoal(Goal goal, long employeeId){
        Optional<Employee> employeeOptional = employeeRepository.findById(employeeId);
        if (!employeeOptional.isPresent()){
            throw new InvalidCredential("Employee not found");
        }
        Employee employee = employeeOptional.get();
        //Check if goal with the same name and deadline already exist
        Optional<Goal> existingGoal = goalRepository.findByNameAndDeadlineAndEmployeeId(goal.getName(), goal.getDeadline(), employeeId);
        if(existingGoal.isPresent()){
            throw new InvalidCredential("Goal with the same name and deadline already assigned to "+ goal.getEmployees().getFirstName());
        }
        //create a new instance of a goal entity
        goal.setEmployees(employee);
        goal.setStatus("Pending");
        Goal savedGoal = goalRepository.save(goal);
        employee.getGoal().add(savedGoal);
        employeeRepository.save(employee);


        //send notification to Employee here

        String messageToEmployee = "New goal Assigned to you. Please review ";
        notificationService.submitNotificationToEmployee(employeeId, messageToEmployee);;


        return savedGoal;

    }




    /**
     * Method to accept a goal
     * @param goalId
     * @return
     */

    public Goal acceptGoal(Long goalId){
        //retrieve the goal from the goal repository
        Goal goal = goalRepository.findById(goalId).get();
        //set the status to "accepted
        goal.setStatus("Accepted");

        //send notification to manager here

        Employee employee = goal.getEmployees();
        Long managerId = employee.getManagerId();
        String messageToManager = goal.getEmployees().getFirstName() + ", " + goal.getEmployees().getLastName()
                +" accepted the assigned goal  " + goal.getName() + ", deadline: " + goal.getDeadline();
        notificationService.submitNotificationToManager(managerId, messageToManager );

        return goalRepository.save(goal);

    }

    /**
     * Method to let employee negotiate about the assigned goal
     * @param goalId
     * @param comments
     * @return
     */

    public Goal negotiateGoal(Long goalId, String comments){
        //retrieve data from the goal repository
        Goal goal = goalRepository.findById(goalId).get();
        //set the comment
        goal.setComments(comments);
        goal.setStatus("Returned back for a review");


        //send notification to manager here
        Employee employee = goal.getEmployees();
        Long managerId = employee.getManagerId();
        String messageToManager = goal.getEmployees().getFirstName() + ", " + goal.getEmployees().getLastName()
                +" returned back the assigned " + goal.getName() + "goal back for more considerations, please review";
        notificationService.submitNotificationToManager(managerId, messageToManager);

        return goalRepository.save(goal);
    }

    /**
     * Update a goal if only the status is not "Accepted"
     * @param goalId
     * @param goal
     * @return
     */

    public Goal updateGoal(Long goalId, Goal goal){
        //retrieve the goal from the repository
        Goal existedGoal = goalRepository.findById(goalId).get();

        //update the goal
        if(existedGoal.getStatus() != "Accepted") {
            existedGoal.setDeadline(goal.getDeadline());
            existedGoal.setName(goal.getName());
            existedGoal.setWeightage(goal.getWeightage());
            existedGoal.setDescription(goal.getDescription());
            existedGoal.setComments(goal.getComments());

            //send notification to employee here
            Long employeeId = existedGoal.getEmployeeId();
            String messageToEmployee = "New goal Assigned to you. Please review ";
            notificationService.submitNotificationToEmployee(employeeId, messageToEmployee);;


            return goalRepository.save(existedGoal);

        }
        else {
            throw new InvalidCredential("Accepted Goal cannot be updated!");
        }
    }
//    For Testing
    public List<Goal> getGoals() {
        return goalRepository.findAll();
    }

    /**
     * Return goal by employee id
     *
     * @param employeeId
     * @return
     */

    public List<Goal> getGoalByEmployeeId(long employeeId, long personal){
      return goalRepository.findByEmployeeId(employeeId, personal);
    }

    /**
     * Get List of Goal By Manager Id
     * @param managerId
     * @return
     */
    public List<Goal> getGoalByManagerId(long managerId){
        return goalRepository.findByEmployees_ManagerId(managerId);
    }

    /**
     * Get List of All Goals
     * @return
     */
    public List<Goal> getAllGoal(){
        return goalRepository.findAll();
    }

    /**
     * Get Goal By Id
     * @param goalId
     * @return
     */
    public Goal getGoalById(long goalId){
        return goalRepository.findById(goalId).get();
    }


}
