-- Documentary subset of SVN r348 mysql.sql; import into a modelling tool only.
-- Original licenses remain in code/plandora/reference/r348.
-- code/plandora/reference/r348/mysql.sql:296-305
create table planning (
	id varchar(10) not null,
	description text,
	creation_date timestamp not null,
	final_date timestamp null,
	iteration varchar(10) null,
 	rich_text_desc text null,
	visible varchar(1) null,
primary key pk_planning (id)
);

-- code/plandora/reference/r348/mysql.sql:614-621
create table plan_relation (
	planning_id varchar(10) not null,
	plan_related_id varchar (10) not null,
	plan_type varchar(2) not null,
	plan_related_type varchar(2) not null,
	relation_type varchar(2) not null,
primary key pk_plan_relation (planning_id, plan_related_id)
);

-- code/plandora/reference/r348/mysql.sql:235-250
create table project (
	id varchar(10) not null,
	name varchar (30),
	parent_id varchar (10),
	can_alloc varchar(1) null,
	project_status_id varchar (10) not null,
	repository_url varchar (200),
	repository_class varchar (200),
	repository_user varchar(40),
	repository_pass varchar(40),
	estimated_closure_date timestamp null,
	allow_billable int(1) null,
	company_id varchar(10) null,
	budget bigint(20) NOT NULL,
primary key pk_project (id)
) ;

-- code/plandora/reference/r348/mysql.sql:209-215
create table project_status (
	id varchar(10) not null,
	name varchar (50),
	note varchar (100),
	state_machine_order bigint(4) ,
primary key pk_proj_status (id)
);

-- code/plandora/reference/r348/mysql.sql:317-322
create table project_history (
	project_id varchar (10) not null,
	project_status_id varchar (10) not null,
	creation_date timestamp not null,
primary key pk_prj_hist (project_id, project_status_id, creation_date)
);

-- code/plandora/reference/r348/mysql.sql:252-264
create table requeriment (
	id varchar(10) not null,
	suggested_date timestamp null,
	deadline_date timestamp null,	
	project_id varchar (10) not null,
	user_id varchar (10) not null,
	requeriment_status_id varchar (10) not null,
	priority int(1) not null,
	is_acceptance int(1) null,
	category_id varchar (10) null,
	reopening bigint(6) null,
primary key pk_req (id)
);

-- code/plandora/reference/r348/mysql.sql:217-225
create table requeriment_status (
	id varchar(10) not null,
	name varchar (50),
	description varchar (100),
	state_machine_order bigint(4),
	accept_project_id varchar(10) null,
	parent_id varchar(10) null,
primary key pk_req_status (id)
);

-- code/plandora/reference/r348/mysql.sql:307-315
create table requeriment_history (
	requeriment_id varchar (10) not null,
	requeriment_status_id varchar (10) not null,
	creation_date timestamp not null,
	user_id varchar (10),	
	iteration varchar(10) null,
	comment text,		
primary key pk_req_hist (requeriment_id, requeriment_status_id, creation_date)
);

-- code/plandora/reference/r348/mysql.sql:266-277
create table task (
	id varchar(10) not null,
	name varchar (50),
	project_id varchar (10) not null,
	requeriment_id varchar (10),
	category_id varchar (10) not null,
	task_id varchar (10),
	is_parent_task int(1),
	created_by varchar (10) null,
	is_unpredictable int(1) null,
primary key pk_task (id)
);

-- code/plandora/reference/r348/mysql.sql:227-233
create table task_status (
	id varchar(10) not null,
	name varchar (50),
	description varchar (100),
	state_machine_order bigint(4),
primary key pk_task_status (id)
);

-- code/plandora/reference/r348/mysql.sql:324-338
create table task_history (
	task_id varchar (10) not null,
	resource_id varchar (10) not null,
	project_id varchar (10) not null,
	task_status_id varchar (10) not null,
	creation_date timestamp not null,	
	start_date timestamp not null,
	estimated_time bigint(6) not null,
	actual_date timestamp null,
	actual_time bigint(6) null,		
	user_id varchar (10),
	iteration varchar(10) null,	
	comment text,
primary key pk_task_hist (task_id, resource_id, project_id, task_status_id, creation_date)
);

-- code/plandora/reference/r348/mysql.sql:279-293
create table resource_task (
	task_id varchar (10) not null,
	resource_id varchar (10) not null,
	project_id varchar (10) not null,
	start_date timestamp not null,
	estimated_time bigint(6) not null,
	actual_date timestamp null,
	actual_time bigint(6) null,	
	task_status_id varchar (10) not null,
	is_acceptance_task int(1),
	billable int(1) null,
	approver_id varchar (10) null,
	approved int(1) null,
primary key pk_resourcetask (task_id, resource_id, project_id)
);

-- code/plandora/reference/r348/mysql.sql:340-347
create table resource_task_alloc (
	task_id varchar (10) not null,
	resource_id varchar (10) not null,
	project_id varchar (10) not null,
	sequence bigint(6) not null,
	alloc_time bigint(6) not null,
primary key pk_resourcetaskalloc (task_id, resource_id, project_id, sequence)
);

-- code/plandora/reference/r348/mysql.sql:349-356
create table resource_task_alloc_plann (
	task_id varchar (10) not null,
	resource_id varchar (10) not null,
	project_id varchar (10) not null,
	sequence bigint(6) not null,
	alloc_time bigint(6) not null,
primary key pk_resourcetaskallocplann (task_id, resource_id, project_id, sequence)
);

-- code/plandora/reference/r348/mysql.sql:742-751
create table resource_capacity (
	resource_id varchar(10) NOT NULL,
	project_id varchar(10) NOT NULL,
    cap_year int(4) NOT NULL,
	cap_month int(2) NOT NULL,
	cap_day int(2) NOT NULL,
	capacity int(4) NOT NULL,
	cost_per_hour int(10) NULL,
primary key pk_resource_capacity (resource_id, project_id, cap_year, cap_month, cap_day)	
);

-- code/plandora/reference/r348/mysql.sql:112-133
create table tool_user (
	id varchar(10) not null,
	username varchar(30) not null,
	password varchar(70) null,
	name varchar(50) not null,
	email varchar(70),
	phone varchar(20),
	color varchar(10),
	department_id varchar(10) not null,
	area_id varchar(10) not null,
	function_id varchar(10) null,
	company_id varchar(10) null,
	country varchar(2) not null,
	language varchar(2) not null,
	auth_mode varchar(200) null,
	birth date null,
	permission text null,
	pic_file mediumblob null,
	final_date timestamp null,
	creation_date timestamp null,	
primary key pk_user (id) 
);

-- code/plandora/reference/r348/mysql.sql:171-183
create table customer (
	id varchar (10) not null,
	project_id varchar (10) not null,
	is_disable int(1) null,	
	is_req_acceptable int(1) null,
	can_see_tech_comment int(1) null,
    pre_approve_req int(1) null,
	can_see_discussion int(1) null,
	function_id varchar(10) null,
	can_see_other_reqs int(1) null,
	can_open_otherowner_reqs int(1) null,
primary key pk_customer (id, project_id)
);

-- code/plandora/reference/r348/mysql.sql:185-195
create table resource (
	id varchar (10) not null,
	project_id varchar (10) not null,
	can_self_alloc int(1) null,
	can_see_repository int(1) null,
	can_see_invoice int(1) null,
	cost_per_hour decimal(9,2) null,
	can_see_customer int(1) null,
	capacity_per_day decimal(9,2) null,
primary key pk_resource (id, project_id)
);

-- code/plandora/reference/r348/mysql.sql:197-201
create table leader (
	id varchar (10) not null,
	project_id varchar (10) not null,
primary key pk_leader (id, project_id)
);

-- code/plandora/reference/r348/mysql.sql:100-110
create table company (
	id varchar(10) not null,
	name varchar(25) not null,
	full_name varchar(255) null,
	company_number varchar(255) null,
	address varchar(255) null,
	city varchar(255) null,
	state_province varchar(50) null,
	country varchar(100) null,
primary key pk_company (id) 
);

-- code/plandora/reference/r348/mysql.sql:135-140
create table department (
	id varchar(10) not null,
	name varchar (50),
	description varchar (100),
primary key pk_department (id) 
);

-- code/plandora/reference/r348/mysql.sql:149-154
create table area (
	id varchar(10) not null,
	name varchar (50),
	description varchar (100),
primary key pk_area (id)
);

-- code/plandora/reference/r348/mysql.sql:142-147
create table function (
	id varchar(10) not null,
	name varchar (50),
	description varchar (100),
primary key pk_function (id)
);

-- code/plandora/reference/r348/mysql.sql:156-169
create table category (
	id varchar(10) not null,
	name varchar (50),
	description varchar (100),
	type int(2) null,
	project_id varchar (10) null,
    billable int(1) NULL,
	disable_view int(1) NULL,
	is_defect int(1) NULL,
	is_testing int(1) NULL,
	is_developing int(1) NULL,
	category_order int(2) null,
primary key pk_category (id)
);

alter table tool_user add constraint user_department foreign key (department_id) references department (id);
alter table tool_user add constraint user_area foreign key (area_id) references area (id);
alter table tool_user add constraint user_function foreign key (function_id) references function (id); 
alter table tool_user add constraint user_company foreign key (company_id) references company (id);
alter table customer add constraint customer_user foreign key (id) references tool_user (id);
alter table resource add constraint resource_customer foreign key (id, project_id) references customer (id, project_id);
alter table leader add constraint leader_resource foreign key (id, project_id) references resource (id, project_id);
alter table customer add constraint customer_project foreign key (project_id) references project (id);
alter table project add constraint project_parent_project foreign key (parent_id) references project (id); 
alter table project add constraint project_status_project foreign key (project_status_id) references project_status (id); 
alter table project add constraint proj_company foreign key (company_id) references company (id);
alter table requeriment add constraint req_parent foreign key (id) references planning (id); 
alter table requeriment add constraint req_cat foreign key (category_id) references category (id); 
alter table project add constraint project_parent foreign key (id) references planning (id); 
alter table requeriment add constraint req_project foreign key (project_id) references project (id); 
alter table requeriment add constraint req_status_req foreign key (requeriment_status_id) references requeriment_status (id); 
alter table requeriment add constraint req_user foreign key (user_id) references tool_user (id); 
alter table requeriment_history add constraint req_hist_req foreign key (requeriment_id) references requeriment (id); 
alter table requeriment_history add constraint req_hist_status foreign key (requeriment_status_id) references requeriment_status (id); 
alter table requeriment_history add constraint req_hist_user foreign key (user_id) references tool_user (id); 
alter table project_history add constraint prj_hist_prj foreign key (project_id) references project (id); 
alter table project_history add constraint prj_hist_status foreign key (project_status_id) references project_status (id); 
alter table task add constraint task_parent foreign key (id) references planning (id); 
alter table task add constraint task_proj foreign key (project_id) references project (id); 
alter table task add constraint task_req foreign key (requeriment_id) references requeriment (id); 
alter table task add constraint task_cat foreign key (category_id) references category (id); 
alter table task add constraint task_task foreign key (task_id) references task (id); 
alter table resource_task add constraint task_res_task foreign key (task_id) references task (id); 
alter table resource_task add constraint res_res_task foreign key (resource_id, project_id) references resource (id, project_id); 
alter table resource_task add constraint res_task_status_task foreign key (task_status_id) references task_status (id); 
alter table resource_task add constraint approver_res_task_fk foreign key (approver_id) references tool_user (id); 
alter table task_history add constraint task_hist_res_task foreign key (task_id, resource_id, project_id) references resource_task (task_id, resource_id, project_id); 
alter table task_history add constraint task_hist_status_task foreign key (task_status_id) references task_status (id);
alter table task_history add constraint task_hist_user foreign key (user_id) references tool_user (id); 
alter table resource_task_alloc add constraint alloc_res_task foreign key (task_id, resource_id, project_id) references resource_task (task_id, resource_id, project_id); 
alter table resource_task_alloc_plann add constraint alloc_plan_res_task foreign key (task_id, resource_id, project_id) references resource_task (task_id, resource_id, project_id); 
alter table category add constraint cat_project foreign key (project_id) references project (id); 
alter table plan_relation add constraint plan_plan_relation foreign key (planning_id) references planning (id);
alter table resource_capacity add constraint res_cap_resource foreign key (resource_id, project_id) references resource (id, project_id);
