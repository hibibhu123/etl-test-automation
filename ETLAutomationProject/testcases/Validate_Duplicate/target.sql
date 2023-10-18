select empno, ename, job,mgr,hiredate,sal,comm,deptno, count(*) from t_emp group by
empno, ename, job,mgr,hiredate,sal,comm,deptno  having count(*) >1