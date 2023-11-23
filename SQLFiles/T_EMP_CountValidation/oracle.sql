SELECT count(*) FROM emp e1 
INNER JOIN dept d1 ON e1.deptno = d1.deptno
INNER JOIN emp e2 on e2.empno=e1.mgr
