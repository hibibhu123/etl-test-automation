select rollno as roll_no, substr(sname,1, instr(sname,' ')-1) as student_f_name,
substr(sname, instr(sname, ' ')+1) as student_l_name, 
case
 when clas='IX' then 9
 when clas='VIII' then 8
 when clas='X' then 10
 end as clas, contactno as contact_no from s_school
