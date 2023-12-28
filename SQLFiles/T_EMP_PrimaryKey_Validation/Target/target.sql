select count(*) as Null_and_Duplicates from T_emp where EMP_ID is null
union
select e.cnt from(
select emp_id, count(*) cnt from t_emp group by emp_id having cnt>1)e