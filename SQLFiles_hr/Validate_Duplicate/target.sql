select employee_id, first_name, last_name,email,phone_number,hire_date,job_id,
salary,commission_pct,manager_id,department_id, count(*) 
from employees group by
employee_id, first_name, last_name,email,phone_number,hire_date,job_id,
salary,commission_pct,manager_id,department_id
having count(*) >1