SELECT 
    e1.EMPNO AS EMP_ID,
    INITCAP(e1.ename) AS EMP_NAME,
    d1.dname AS DEPT_NAME,
    INITCAP(e2.ename) AS MANAGER_NAME,
    e1.job AS JOB_TYPE,
    INITCAP(d1.loc) AS LOCATION,
    TO_CHAR(e1.hiredate,'yyyy-mm-dd') as HIRE_DATE,
    ROUND(MONTHS_BETWEEN(SYSDATE, e1.hiredate) / 12) AS TOTAL_EXP_IN_COMPANY,
    e1.sal AS SALARY,
    NVL(e1.comm, 0) AS COMMISSION,
    e1.sal + NVL(e1.comm, 0) AS TOTAL_SALARY,
    CASE 
        WHEN e1.sal + NVL(e1.comm, 0) < 2000 THEN 'LOW' 
        WHEN e1.sal + NVL(e1.comm, 0) BETWEEN 2000 AND 3000 THEN 'MID' 
        ELSE 'HIGH' 
    END AS SALARY_GRADE
FROM s_emp e1 
INNER JOIN s_dept d1 ON e1.deptno = d1.deptno
INNER JOIN s_emp e2 on e2.empno=e1.mgr
order by EMP_ID