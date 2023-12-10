SELECT count(*) FROM s_emp e1 
INNER JOIN s_dept d1 ON e1.deptno = d1.deptno
INNER JOIN s_emp e2 on e2.empno=e1.mgr
