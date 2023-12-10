select count(*) as Null_and_Duplicates from T_emp where T_EMP_ID is null
union
select e.cnt from(
select t_emp_id, count(*) cnt from t_emp group by t_emp_id having cnt>1)e