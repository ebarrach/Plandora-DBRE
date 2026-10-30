drop table if exists project_qualifier_value;
drop table if exists user_edi_link; 
drop table if exists external_data_attach;
drop table if exists external_data;
drop table if exists artifact_template;
drop table if exists artifact;
drop table if exists db_repository_version;
drop table if exists db_repository_item;
drop table if exists db_repository_sequence;
drop table if exists expense;
drop table if exists cost_history;
drop table if exists cost_installment;
drop table if exists cost;
drop table if exists cost_status;
drop table if exists invoice_history;
drop table if exists invoice_item;
drop table if exists invoice;
drop table if exists invoice_status;
drop table if exists repository_history;
drop table if exists repository_file_plan;
drop table if exists repository_file_project;
drop table if exists repository_file;
drop table if exists customer_function;
drop table if exists resource_capacity;
drop table if exists question_answer;
drop table if exists question_alternative;
drop table if exists survey_question;
drop table if exists survey;
drop table if exists custom_form;
drop table if exists meta_form;
drop table if exists custom_node_template;
drop table if exists step_node_template;
drop table if exists decision_node_template;
drop table if exists node_template;
drop table if exists template;
drop table if exists plan_relation;
drop table if exists risk_history;
drop table if exists risk;
drop table if exists risk_status;
drop table if exists attachment_history;
drop table if exists attachment;
drop table if exists occurrence_field_table;
drop table if exists occurrence_kpi;
drop table if exists occurrence_history;
drop table if exists occurrence_field;
drop table if exists occurrence_dependency; 
drop table if exists occurrence;
drop table if exists additional_table;
drop table if exists additional_field;
drop table if exists meta_field;
drop table if exists preference;
drop table if exists report_result;
drop table if exists project_report;
drop table if exists report;
drop table if exists notification_field;
drop table if exists notification;
drop table if exists resource_task_alloc;
drop table if exists resource_task_alloc_plann;
drop table if exists task_history;
drop table if exists project_history;
drop table if exists requeriment_history;
drop table if exists resource_task;
drop table if exists task;
drop table if exists requeriment;
drop table if exists task_status;
drop table if exists requeriment_status;
drop table if exists root;
drop table if exists leader;
drop table if exists resource;
drop table if exists customer;
drop table if exists discussion_topic;
drop table if exists discussion;
drop table if exists category;
drop table if exists repository_policy;
drop table if exists project;
drop table if exists project_status;
drop table if exists planning;
drop table if exists tool_user;
drop table if exists area;
drop table if exists function;
drop table if exists department;
drop table if exists company;
drop table if exists event_log;

drop table if exists p_sequence;


create table p_sequence (
	id bigint(9) not null, 
	primary key pk_p_sequence (id)
);

create table event_log (
	summary varchar(10) not null,
	description text,
	creation_date timestamp not null,	
	username varchar(30) not null
);

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
 
create table department (
	id varchar(10) not null,
	name varchar (50),
	description varchar (100),
primary key pk_department (id) 
);

create table function (
	id varchar(10) not null,
	name varchar (50),
	description varchar (100),
primary key pk_function (id)
);

create table area (
	id varchar(10) not null,
	name varchar (50),
	description varchar (100),
primary key pk_area (id)
);

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

create table leader (
	id varchar (10) not null,
	project_id varchar (10) not null,
primary key pk_leader (id, project_id)
);

create table root (
	id varchar (10) not null,
	project_id varchar (10) not null,
primary key pk_root (id, project_id)
);

create table project_status (
	id varchar(10) not null,
	name varchar (50),
	note varchar (100),
	state_machine_order bigint(4) ,
primary key pk_proj_status (id)
);

create table requeriment_status (
	id varchar(10) not null,
	name varchar (50),
	description varchar (100),
	state_machine_order bigint(4),
	accept_project_id varchar(10) null,
	parent_id varchar(10) null,
primary key pk_req_status (id)
);

create table task_status (
	id varchar(10) not null,
	name varchar (50),
	description varchar (100),
	state_machine_order bigint(4),
primary key pk_task_status (id)
);

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

create table requeriment_history (
	requeriment_id varchar (10) not null,
	requeriment_status_id varchar (10) not null,
	creation_date timestamp not null,
	user_id varchar (10),	
	iteration varchar(10) null,
	comment text,		
primary key pk_req_hist (requeriment_id, requeriment_status_id, creation_date)
);

create table project_history (
	project_id varchar (10) not null,
	project_status_id varchar (10) not null,
	creation_date timestamp not null,
primary key pk_prj_hist (project_id, project_status_id, creation_date)
);

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

create table resource_task_alloc (
	task_id varchar (10) not null,
	resource_id varchar (10) not null,
	project_id varchar (10) not null,
	sequence bigint(6) not null,
	alloc_time bigint(6) not null,
primary key pk_resourcetaskalloc (task_id, resource_id, project_id, sequence)
);

create table resource_task_alloc_plann (
	task_id varchar (10) not null,
	resource_id varchar (10) not null,
	project_id varchar (10) not null,
	sequence bigint(6) not null,
	alloc_time bigint(6) not null,
primary key pk_resourcetaskallocplann (task_id, resource_id, project_id, sequence)
);

create table report (
	id varchar(10) not null,
	name varchar(100) null,
	description text null,
	type int(2) not null,
	kpi_type int(2) null,
	report_perspective_id varchar(10) not null,
	sql_text text null,
	execution_hour int(2) not null,
	last_execution timestamp null,
	project_id varchar(10) not null,
	final_date timestamp null,
	data_type int(2) not null,
	file_name varchar(100) null,
	category_id varchar(10) null,
	profile_view varchar(1) null,
	goal varchar(25) null,
	tolerance varchar(25) null,
	tolerance_type varchar(2) null,
primary key pk_report (id)
);


create table report_result (
	report_id varchar(10) not null,
	project_id varchar(10) not null,
	last_execution timestamp null,
	value varchar(25) not null,
primary key pk_report_result (report_id, project_id, last_execution)
);


create table project_report (
	report_id varchar(10) not null,
	project_id varchar(10) not null,
primary key pk_project_report (report_id, project_id)
);


create table notification (
	id varchar(10) not null,
	name varchar(50) not null,
	description varchar(100) null,	
	notification_class varchar(100) not null,
	sql_text text not null,
	retry_number int(2) not null,
	next_notification timestamp not null,
	final_date timestamp null,
	last_check timestamp not null,	
	period_minute int(3) null,	
	period_hour int(3) null,		
	periodicity int(2) null not null,
primary key pk_notification (id)
);

create table notification_field (
	notification_id varchar(10) not null,
	name varchar(100) not null,
	value text null,
primary key pk_notif_field (notification_id, name)
);

create table preference (
	user_id varchar(10) not null,
	id varchar(50) null,
	value text not null,
primary key pk_preference (user_id, id)
);

create table meta_field (
	id varchar(10) not null,
	name varchar(50) null,
	type int(1) not null,
	apply_to int(1) not null,	
	project_id varchar (10) not null,	
	category_id varchar (10) null,		
	domain text not null,
	final_date timestamp null,
	meta_form_id varchar(10) null,
	help_content varchar(300) null,
	field_order int(2) null,	
	is_mandatory varchar(1),
	is_qualifier varchar(1),
primary key pk_metafield (id)
);

create table additional_field (
	planning_id varchar(10) not null,
	meta_field_id varchar (10) not null,	
	value text not null,
	date_value timestamp null,
	numeric_value decimal(13,2) null,
	modification_date timestamp null,
primary key pk_additional_field (planning_id, meta_field_id)
);

create table additional_table (
	planning_id varchar(10) not null,
	meta_field_id varchar (10) not null,	
    line int(6) not null,
    col int(6) not null,
	value text not null,
	date_value timestamp null,
primary key pk_additional_table (planning_id, meta_field_id, line, col)
);

create table occurrence (
	id varchar(10) not null,
	source varchar(100) not null,
	project_id varchar(10) not null,
	name varchar(255) null,
	loc varchar(7) null,
	occurrence_status varchar(10) null,	
	occurrence_status_label varchar(60) null,
primary key pk_occurrence (id)
);

create table occurrence_dependency (
	occurrence_id varchar(10) not null,
	planning_id varchar(10) not null,
primary key pk_occur_dependency (occurrence_id, planning_id)
);

create table occurrence_field (
	occurrence_id varchar(10) not null,
	field varchar(10) not null,	
	value text not null,
	date_value timestamp null,
primary key pk_occur_field (occurrence_id, field)
);

create table occurrence_field_table (
	occurrence_id varchar(10) not null,
	occ_field varchar(10) not null,
	field varchar(10) not null,	
    line int(6) not null,
    col int(6) not null,
	value text null,
	date_value timestamp null,
primary key pk_occurrence_field_table (occurrence_id, occ_field, field, line, col)
);

create table occurrence_history (
	occurrence_id varchar (10) not null,
	occurrence_status varchar (10) not null,
	occurrence_status_label varchar (60) not null,	
	creation_date timestamp not null,
	user_id varchar (10) not null,
	history TEXT,
primary key pk_occur_history (occurrence_id, occurrence_status, creation_date, user_id)
);

create table occurrence_kpi (
	report_id varchar(10) not null,
	occurrence_id varchar(10) not null,
	creation_date timestamp null,
	weight int(1) null,
primary key pk_occurrence_kpi (report_id, occurrence_id)
);

        
create table risk (
	id varchar (10) not null,
	project_id varchar(10) not null,
	category_id varchar(10) not null,
	name varchar(50) not null,
	probability varchar(2) not null,
	impact varchar(2) not null,
	tendency varchar(2) not null,	
	responsible varchar(40) null,	
	risk_status_id varchar (10) not null,
	strategy TEXT null,	
	contingency TEXT null,
	impact_cost varchar(1) null,
	impact_time varchar(1) null,
	impact_quality varchar(1) null,
	impact_scope varchar(1) null,
	risk_type varchar(1) null,
primary key pk_risk (id)
);

create table risk_history (
	risk_id varchar (10) not null,
	risk_status_id varchar (10) not null,
	risk_status_label varchar (60) null,	
	creation_date timestamp not null,
	user_id varchar (10) not null,
	history TEXT,
	probability varchar(2) null,
	impact varchar(2) null,
	tendency varchar(2) null,	
	impact_cost varchar(1) null,
	impact_time varchar(1) null,
	impact_quality varchar(1) null,
	impact_scope varchar(1) null,
	risk_type varchar(1) null,	
	name text null,
	description text null,
	responsible varchar(40) null,
    category_id varchar(10) null,
    strategy TEXT null,
    contingency TEXT null,	
primary key pk_risk_history (risk_id, risk_status_id, creation_date, user_id)
);

create table risk_status (
	id varchar(10) not null,
	name varchar (50),
	description varchar (100),
	status_type varchar(1) not null,
primary key pk_risk_status (id)
);

create table attachment (
	id varchar(10) not null,
	planning_id varchar (10) null,
	name varchar (255),
	status varchar(2) not null,
	visibility varchar(2) not null,
	type varchar(40) not null,
	content_type varchar(255) not null,	
	creation_date timestamp not null,	
	comment text null,
	binary_file mediumblob not null,
primary key pk_attachment (id)
);

create table attachment_history (
	attachment_id varchar(10) not null,
	status varchar(2) not null,
	creation_date timestamp not null,	
	user_id varchar(10) not null,
	history TEXT,
primary key pk_attach_history (attachment_id, status, creation_date, user_id)
);

create table discussion_topic (
	id varchar(10) not null,
	planning_id varchar (10) not null,
	content text null,
	parent_topic varchar(10) null,
	user_id varchar(10) null,
	creation_date timestamp not null,
primary key pk_discussion_topic (id)
);

create table discussion (
	id varchar(10) not null,
	project_id varchar (10) null,
	name varchar (255) not null,
	owner varchar(10) null,
	category_id varchar(10) not null,
	is_blocked int(1) null,
primary key pk_discussion (id)
);

create table plan_relation (
	planning_id varchar(10) not null,
	plan_related_id varchar (10) not null,
	plan_type varchar(2) not null,
	plan_related_type varchar(2) not null,
	relation_type varchar(2) not null,
primary key pk_plan_relation (planning_id, plan_related_id)
);

create table template (
	id varchar(10) not null,
	name varchar(50) not null,
	deprecated_date timestamp null,
	root_node_id varchar(10) not null,
	category_id varchar(10) null,
primary key pk_template (id)
);

create table node_template (
	id varchar(10) not null,
	name varchar(50) not null,
	description text null,
	node_type varchar(2) not null,
	planning_id varchar(10) not null,
	project_id varchar (10) null,	
	next_node_id varchar(10) null,	
primary key pk_node_template (id)
);

create table step_node_template (
	id varchar(10) not null,
	category_id varchar(10) not null,
	resource_list text null,	
	category_regex varchar(40) null,
	iteration varchar(10) null,
primary key pk_step_node_template (id)
);

create table decision_node_template (
	id varchar(10) not null,
	question_content text null,
	next_node_id_if_false varchar(10) null,
primary key pk_decision_node_template (id)
);

create table custom_node_template (
	node_template_id varchar(10) not null,
	template_id varchar(10) not null,
	instance_id varchar(10) not null,	
	name varchar(50) not null,
	description text null,
	planning_id varchar(10) null,
	project_id varchar (10) null,	
	category_id varchar(10) null,
	related_task_id varchar(10) null,
	resource_list text null,
	question_content text null,
	is_parent_task integer null,
	decision_answer text null,
	iteration varchar(10) null,
primary key pk_custom_node_template (node_template_id, template_id, instance_id)
);


create table meta_form (
	id varchar(10) not null,
	name varchar (30) not null,
	viewable_cols text null,
	grid_row_num integer NULL,
	filter_col_id varchar(10) null,
	js_before_save text null,
	js_after_save text null,
	js_after_load text null,
	save_batch integer null,
	constraint_edit_delete integer null,
primary key pk_meta_form (id)
);

create table custom_form (
	id varchar(10) not null,
	meta_form_id varchar (10) not null,
	creation_user_id varchar(10) null,
	modification_user_id varchar(10) null,
primary key pk_custom_form (id)
);

create table survey (
	id varchar(10) NOT NULL,
	name varchar(50) NOT NULL,
	description TEXT NULL,
	is_template varchar(1) NOT NULL,
	is_anonymous varchar(1) NOT NULL,
	project_id varchar(10) NOT NULL,
	creation_date timestamp NOT NULL,
	final_date timestamp NULL,
	date_publishing timestamp NOT NULL,
	anonymous_key varchar(70) NOT NULL,
primary key pk_survey (id) 
);

create table survey_question (
	survey_id varchar(10) NOT NULL,
	id varchar(10) NOT NULL,
	type varchar(1) NOT NULL,
	content TEXT NOT NULL,
	position integer NOT NULL,
	sub_title varchar(50) NULL,
	is_mandatory varchar(1) NULL,
primary key pk_survey_question (survey_id, id) 	
);

create table question_alternative (
	survey_id varchar(10) NOT NULL,
	question_id varchar(10) NOT NULL,
	sequence integer NOT NULL,
	content TEXT NOT NULL,
primary key pk_survey_question (survey_id, question_id, sequence)
);

create table question_answer (
	survey_id varchar(10) NOT NULL,
	question_id varchar(10) NOT NULL,
    answer_date timestamp NOT NULL,
	user_id varchar(10) NULL,
	value TEXT NOT NULL,
primary key pk_question_answer (survey_id, question_id, answer_date)	
);

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

create table customer_function (
	customer_id varchar(10) NOT NULL,
	project_id varchar(10) NOT NULL,
	function_id varchar(10) NOT NULL,
	creation_date timestamp null,	
primary key pk_customer_function (customer_id, project_id, function_id)	
);

create table repository_file (
	id varchar(10) NOT NULL,
	repository_file_path TEXT NOT NULL,
primary key pk_rep_file (id)	
);

create table repository_file_project (
	repository_file_id varchar(10) NOT NULL,
	project_id varchar(10) NOT NULL,
	is_disable int(1) NULL,
    is_indexable int(1) NULL,
    is_downloadable int(1) null,
	last_update timestamp NOT NULL,	
primary key pk_rep_file_project (repository_file_id, project_id)	
);

create table repository_file_plan (
	repository_file_id varchar(10) NOT NULL,
	planning_id varchar(10) NOT NULL,
primary key pk_rep_file_plan (repository_file_id, planning_id)	
);

create table repository_history (
	repository_file TEXT NOT NULL,
	repository_file_path TEXT NOT NULL,
	creation_date timestamp NOT NULL,
	project_id varchar(10) NOT NULL,
	user_id varchar(10) NULL,
	hist_type varchar(10) NOT NULL,
	comment TEXT NULL	
);

create table cost_status (
	id varchar(10) not null,
	name varchar (50) not null,
	description varchar (100),
	state_machine_order bigint(4) not null,
primary key pk_cost_status (id)
);

create table cost (
	id varchar(10) NOT NULL,
	name varchar(30) NOT NULL,
	project_id varchar(10) NOT NULL,
    category_id varchar(10) NOT NULL,
	account_code varchar(30) NULL,
	expense_id varchar(10) NULL,
primary key pk_cost (id)	
);

create table cost_installment (
	cost_id varchar(10) NOT NULL,
	installment_num int(4) NOT NULL,
	due_date timestamp NOT NULL,
	cost_status_id varchar(10) NOT NULL,
	approver varchar(10) NULL,
	value bigint(20) NOT NULL,
primary key pk_cost_installment (cost_id, installment_num)	
);

create table cost_history (
	cost_id varchar(10) NOT NULL,
	installment_num int(4) NOT NULL,
	creation_date timestamp NOT NULL,
	name varchar(30) NOT NULL,
	project_id varchar(10) NOT NULL,
    category_id varchar(10) NOT NULL,
	account_code varchar(30) NULL,
	expense_id varchar(10) NULL,
	due_date timestamp NOT NULL,
	cost_status_id varchar(10) NOT NULL,
	value bigint(20) NOT NULL,
	user_id varchar(10) NULL,	
primary key pk_cost_history (cost_id, installment_num, creation_date)	
);


create table expense (
	id varchar(10) NOT NULL,
	project_id varchar(10) NOT NULL,
    user_id varchar(10) NOT NULL,
primary key pk_expense (id)
);

create table invoice_status (
	id varchar(10) not null,
	name varchar (50),
	description varchar (100),
	state_machine_order bigint(4),
primary key pk_invoice_status (id)
);

create table invoice (
	id varchar(10) NOT NULL,
	name varchar(50) NOT NULL,
	project_id varchar(10) NOT NULL,
    category_id varchar(10) NOT NULL,
	due_date timestamp NOT NULL,
	invoice_status_id varchar(10) NOT NULL,
	invoice_date timestamp NULL,
	invoice_number varchar(40) NULL,
	purchase_order varchar(40) NULL,
	contact varchar(70) NULL,
primary key pk_invoice (id)	
);

create table invoice_item (
	invoice_id varchar(10) NOT NULL,
	invoice_item_id varchar(10) NOT NULL,
	name varchar(50) null,
	type int(1) NOT NULL,
	price bigint(20) NOT NULL,
    amount int(3) NOT NULL,
    type_index int(2) NOT NULL,
primary key pk_invoice_item (invoice_id, invoice_item_id)	
);

create table invoice_history (
	invoice_id varchar (10) not null,
	creation_date timestamp not null,
	name varchar(50) NOT NULL,
    category_id varchar(10) NOT NULL,
    invoice_status_id varchar(10) NOT NULL,
	due_date timestamp NOT NULL,
	invoice_date timestamp NULL,
	invoice_number varchar(40) NULL,
	purchase_order varchar(40) NULL,
	contact varchar(70) NULL,
	description TEXT NULL,
	total_price bigint(20) NULL,
	user_id varchar (10) not null,
primary key pk_invoice_hist (invoice_id, creation_date)
);

create table db_repository_sequence (
	id int(10) not null,
	project_id varchar(10) NOT NULL, 
primary key pk_rep_sequence (id, project_id)
);

create table db_repository_item (
	id varchar(10) NOT NULL,
	repository_file_path TEXT NOT NULL,
	repository_file_name TEXT NOT NULL,
	project_id varchar(10) NOT NULL,
	is_directory int(1) NULL,
	parent_id varchar(10) NULL,
primary key pk_rep_item (id)	
);

create table db_repository_version (
	repository_item_id varchar(10) not null,
	version int(10) not null,
	content_type varchar(40) not null,	
	creation_date timestamp not null,	
	comment text null,
	user_id varchar (10) not null,
	file_size int(13) not null,
	binary_file mediumblob null,
primary key pk_rep_version (repository_item_id, version)	
);

create table repository_policy (
	project_id varchar(10) not null,
	type_policy varchar(30) not null,
	value text null,
primary key pk_repos_policies (project_id, type_policy)
);

create table artifact_template (
	id varchar(10) not null,
	name varchar(70) not null,
	description varchar(255) null,
	category_id varchar(10) not null,
	header_html text null,
	body_html text not null,
	footer_html text null,
	profile_view varchar(1) null,
primary key pk_artif_templ (id)
);

create table external_data (
	id varchar(10) not null,
	external_id text not null,
	external_sys varchar(50) not null,
	external_host varchar(255) not null,
	external_accont varchar(255) not null,
	planning_id varchar(10) null,
	msg_date timestamp NULL,	
	event_date timestamp NULL,
	destination text null,
	source text null,
	summary text null,
	body text null,
	priority int(1) null,
	due_date timestamp null,
primary key pk_external_data (id)
);


create table external_data_attach (
	external_data_id varchar(10) not null,
	attachment_id varchar(255) not null,
	content_type varchar(255) not null,	
	creation_date timestamp not null,
	file_size bigint(9) not null,	
	file_name text not null,
	binary_file mediumblob not null,
primary key pk_external_data_attach (external_data_id, attachment_id)	
);


create table artifact (
    id varchar(10) NOT NULL,
	repository_file_id varchar(10) NOT NULL,
	last_update timestamp NOT NULL,	
	artifact_template_type TEXT NULL,
	project_id varchar(10) not null,
primary key pk_artifact (id)
);

create table user_edi_link (
    user_id varchar (10) not null,
	edi_id varchar(10) not null,
	last_update timestamp not null,	
	edi_uuid TEXT not null,
primary key pk_user_edi_link (user_id, edi_id)
);


alter table user_edi_link add constraint user_user_edi_link foreign key (user_id) references tool_user (id);

alter table external_data_attach add constraint ext_data_attach_ext_data foreign key (external_data_id) references external_data (id);

alter table artifact_template add constraint artif_templ_category foreign key (category_id) references category (id);

alter table artifact add constraint reposit_file_artif foreign key (repository_file_id) references repository_file (id);

alter table artifact add constraint artifact_planning foreign key (id) references planning (id);

alter table invoice add constraint invoice_category foreign key (category_id) references category (id);

alter table invoice add constraint invoice_status foreign key (invoice_status_id) references invoice_status (id);

alter table invoice add constraint invoice_planning foreign key (id) references planning (id);

alter table invoice_item add constraint inv_item_invoice foreign key (invoice_id) references invoice (id);

alter table tool_user add constraint user_department foreign key (department_id) references department (id);

alter table tool_user add constraint user_area foreign key (area_id) references area (id);

alter table tool_user add constraint user_function foreign key (function_id) references function (id); 

alter table tool_user add constraint user_company foreign key (company_id) references company (id);

alter table customer add constraint customer_user foreign key (id) references tool_user (id);

alter table resource add constraint resource_customer foreign key (id, project_id) references customer (id, project_id);

alter table leader add constraint leader_resource foreign key (id, project_id) references resource (id, project_id);

alter table root add constraint root_leader foreign key (id, project_id) references leader (id, project_id);

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

alter table report add constraint report_project foreign key (project_id) references project (id); 

alter table report add constraint report_cat foreign key (category_id) references category (id); 

alter table report_result add constraint rep_result_rep_id foreign key (report_id) references report (id); 

alter table notification_field add constraint notif_field_notif_id foreign key (notification_id) references notification (id); 

alter table preference add constraint user_preference foreign key (user_id) references tool_user (id);

alter table meta_field add constraint meta_field_project foreign key (project_id) references project (id); 

alter table meta_field add constraint meta_field_category foreign key (category_id) references category (id); 
	
alter table additional_field add constraint addinfo_field_planning foreign key (planning_id) references planning (id); 

alter table additional_field add constraint addinfo_field_meta_field foreign key (meta_field_id) references meta_field (id); 

alter table category add constraint cat_project foreign key (project_id) references project (id); 

alter table occurrence add constraint occur_project foreign key (project_id) references project (id); 

alter table occurrence add constraint occurrence_parent foreign key (id) references planning (id); 

alter table occurrence_history add constraint occur_occur_hist foreign key (occurrence_id) references occurrence (id); 

alter table occurrence_field add constraint occur_occur_field foreign key (occurrence_id) references occurrence (id); 

alter table occurrence_field_table add constraint occur_occur_field_tbl foreign key (occurrence_id) references occurrence (id);

alter table occurrence_dependency add constraint occur_dependency foreign key (occurrence_id) references occurrence (id); 

alter table occurrence_dependency add constraint occur_depend_planning foreign key (planning_id) references planning (id); 

alter table occurrence_kpi add constraint occur_kpi_occur foreign key (occurrence_id) references occurrence (id); 

alter table occurrence_kpi add constraint occur_kpi_rep foreign key (report_id) references report (id); 

alter table risk add constraint risk_project foreign key (project_id) references project (id); 

alter table risk_history add constraint risk_risk_hist foreign key (risk_id) references risk (id); 

alter table risk_history add constraint risk_status_hist foreign key (risk_status_id) references risk_status (id); 

alter table attachment add constraint planning_attach foreign key (planning_id) references planning (id);

alter table attachment_history add constraint attach_hist foreign key (attachment_id) references attachment (id); 

alter table discussion_topic add constraint planning_disc_topic foreign key (planning_id) references planning (id);

alter table discussion_topic add constraint parent_disc_topic foreign key (parent_topic) references discussion_topic (id);

alter table discussion_topic add constraint user_disc_topic foreign key (user_id) references tool_user (id);

alter table discussion add constraint discussion_project foreign key (project_id) references project (id);

alter table discussion add constraint discussion_category foreign key (category_id) references category (id);

alter table plan_relation add constraint plan_plan_relation foreign key (planning_id) references planning (id);

alter table custom_form add constraint custom_meta foreign key (meta_form_id) references meta_form (id);

alter table survey_question add constraint question_survey foreign key (survey_id) references survey (id);

alter table question_alternative add constraint alternat_question foreign key (survey_id, question_id) references survey_question (survey_id, id);

alter table question_answer add constraint answer_question foreign key (survey_id, question_id) references survey_question (survey_id, id);

alter table resource_capacity add constraint res_cap_resource foreign key (resource_id, project_id) references resource (id, project_id);

alter table customer_function add constraint cus_func_customer foreign key (customer_id) references customer (id);

alter table customer_function add constraint cus_func_project foreign key (project_id) references project (id);

alter table customer_function add constraint cus_func_function foreign key (function_id) references function (id);

alter table repository_file_project add constraint proj_rep_file_proj foreign key (project_id) references project (id);

alter table repository_file_project add constraint re_file_rep_file_proj foreign key (repository_file_id) references repository_file (id);

alter table repository_file_plan add constraint rep_file_plan_rep_file foreign key (repository_file_id) references repository_file (id);

alter table repository_file_plan add constraint rep_file_plan_plan foreign key (planning_id) references planning (id);

alter table repository_policy add constraint rep_polic_prj foreign key (project_id) references project (id); 

alter table cost add constraint cost_project foreign key (project_id) references project (id);

alter table cost add constraint cost_planning foreign key (id) references planning (id);

alter table cost_installment add constraint cost_inst_cost foreign key (cost_id) references cost (id);

alter table project_report add constraint proj_rep_report foreign key (report_id) references report (id);

alter table project_report add constraint proj_rep_proj foreign key (project_id) references project (id);

alter table db_repository_version add constraint db_rep_vers_item foreign key (repository_item_id) references db_repository_item (id);

alter table step_node_template add constraint step_node_templ foreign key (id) references node_template (id);

alter table decision_node_template add constraint decis_node_templ foreign key (id) references node_template (id);

insert into area (id,name,description) values ('1','Development','Development'); 
insert into area (id,name,description) values ('10','Purchasing','Purchasing'); 
insert into area (id,name,description) values ('2','Maintenance','Maintenance'); 
insert into area (id,name,description) values ('3','Suport','Suport'); 
insert into area (id,name,description) values ('4','Researching','Researching'); 
insert into area (id,name,description) values ('5','Account','Account'); 
insert into area (id,name,description) values ('6','Selling','Selling'); 
insert into area (id,name,description) values ('7','Customer Care','Customer Care'); 
insert into area (id,name,description) values ('8','Finance','Finance '); 
insert into area (id,name,description) values ('9','Directory','Directory'); 

insert into department (id,name,description) values ('1','Purchasing','Purchasing Department'); 
insert into department (id,name,description) values ('2','Finance','Finance Department'); 
insert into department (id,name,description) values ('3','HR','Human Resouce Department'); 
insert into department (id,name,description) values ('4','IT','IT Department'); 
insert into department (id,name,description) values ('5','Marketing','Marketing Department'); 
insert into department (id,name,description) values ('6','Directory','Directory'); 
insert into department (id,name,description) values ('7','Account','Account Department'); 
insert into department (id,name,description) values ('8','Commercial','Commercial Department'); 

insert into function (id,name,description) values ('1','Purchase Analist','Purchase Analist'); 
insert into function (id,name,description) values ('10','Customer Attendent','Customer Care Attendent'); 
insert into function (id,name,description) values ('11','Finance Analist','Finance Analist'); 
insert into function (id,name,description) values ('12','Secretary','Secretary'); 
insert into function (id,name,description) values ('13','DBA','Data Base administrator'); 
insert into function (id,name,description) values ('14','Support Analist','Support Analist (Helpdesk)'); 
insert into function (id,name,description) values ('15','Computer Programmer','Computer Programmer');
insert into function (id,name,description) values ('16','Final User','Final User');
insert into function (id,name,description) values ('2','Adm.Assistent','Adm.Assistent'); 
insert into function (id,name,description) values ('3','Coordenator','Coordenator'); 
insert into function (id,name,description) values ('4','Diretor','Diretor'); 
insert into function (id,name,description) values ('5','President','President'); 
insert into function (id,name,description) values ('6','Manager','Manager Department'); 
insert into function (id,name,description) values ('7','System Analist','System Analist'); 
insert into function (id,name,description) values ('8','Purchaser','Purchaser'); 
insert into function (id,name,description) values ('9','Seller','Seller'); 

insert into project_status (id,name,note,state_machine_order) values ('1','Open','Open Project',1); 
insert into project_status (id,name,note,state_machine_order) values ('2','Closed','Closed Project',3); 
insert into project_status (id,name,note,state_machine_order) values ('3','Aborted','Aborted Project',3); 
insert into project_status (id,name,note,state_machine_order) values ('4','on-Hold','On-hold Project',2); 

insert into task_status (id,name,description,state_machine_order) values ('1','Open','Open',1); 
insert into task_status (id,name,description,state_machine_order) values ('2','Closed','Close',100); 
insert into task_status (id,name,description,state_machine_order) values ('3','Canceled','Canceled',101); 
insert into task_status (id,name,description,state_machine_order) values ('4','in-Progress','in-Progress',20); 
insert into task_status (id,name,description,state_machine_order) values ('5','on-Hold','on-Hold',50); 
insert into task_status (id,name,description,state_machine_order) values ('6','Reopen','Reopen',2);

insert into requeriment_status (id,name,description,state_machine_order) values ('1','Waiting Approve','Waiting Approve',1); 
insert into requeriment_status (id,name,description,state_machine_order) values ('3','Refused','Refused',202); 
insert into requeriment_status (id,name,description,state_machine_order) values ('4','Closed','Closed',201); 
insert into requeriment_status (id,name,description,state_machine_order) values ('5','Canceled','Canceled',200); 
insert into requeriment_status (id,name,description,state_machine_order) values ('6','Planned','Planned',100); 
insert into requeriment_status (id,name,description,state_machine_order) values ('7','in-Progress','in-Progress', 300); 

insert into tool_user (id,username,password,name,email,phone,color,department_id,area_id,function_id,country,language) values ('2','franz','','Franz Kafka',NULL,NULL,'C0c0c0','2','4','11','BR','pt'); 
insert into tool_user (id,username,password,name,email,phone,color,department_id,area_id,function_id,country,language) values ('3','root','','System Root', 'DO NOT REMOVE THIS USER',NULL,'000000','4','2','7','BR','pt'); 
insert into tool_user (id,username,password,name,email,phone,color,department_id,area_id,function_id,country,language) values ('4','camus','','Albert Camus',NULL,NULL,'1199AA','6','9','3','BR','pt'); 
insert into tool_user (id,username,password,name,email,phone,color,department_id,area_id,function_id,country,language) values ('5','noam','','Noam Chomsky',NULL,NULL,'F1E3AB','4','4','3','BR','pt'); 

insert into invoice_status (id,name,description,state_machine_order) values ('1','Bugdet','Bugdet',1); 
insert into invoice_status (id,name,description,state_machine_order) values ('2','Paid','Paid',100); 
insert into invoice_status (id,name,description,state_machine_order) values ('3','Canceled','Canceled',101); 
insert into invoice_status (id,name,description,state_machine_order) values ('4','Reviewed','Reviewed',40); 
insert into invoice_status (id,name,description,state_machine_order) values ('5','Submitted','Submitted',50); 

insert into planning (id,description,creation_date,final_date) values ('0','Root Project - do NOT remove this project!','2013-01-01 00:00:00',null); 
insert into planning (id,description,creation_date,final_date) values ('1','Help Desk Example Description','2013-01-01 00:00:00',null); 
insert into planning (id,description,creation_date,final_date) values ('2','Development Example Description','2013-01-01 00:00:00',null); 
insert into project (id,name,parent_id,project_status_id,budget) values ('0','',NULL,'1',0); 
insert into project (id,name,parent_id,project_status_id,budget) values ('1','Help Desk Project Example','0','1',0); 
insert into project (id,name,parent_id,project_status_id,budget) values ('2','Development Project Example','0','1',0);

insert into category (id,name,description, type, project_id) values ('0','',NULL,NULL,NULL); -- MANDATORY
insert into category (id,name,description, type, project_id, billable) values ('1','Implementation' ,'',0, NULL, 1); 
insert into category (id,name,description, type, project_id, billable) values ('2','Analisys'       ,'',0, NULL, 1); 
insert into category (id,name,description, type, project_id, billable, is_defect, is_testing) values ('3','Testing','',0, NULL, 0, 0, 1); 
insert into category (id,name,description, type, project_id, billable, is_defect, is_testing) values ('4','Maintenance','',0, NULL, 0, 1, 0); 
insert into category (id,name,description, type, project_id, billable) values ('5','Support Service','',0, NULL, 1); 
insert into category (id,name,description, type, project_id, billable) values ('6','Trainning','',0, NULL, 0); 
insert into category (id,name,description, type, project_id) values ('7','Bug','',1, 2); 
insert into category (id,name,description, type, project_id) values ('8','Enhancement','',1, 2); 
insert into category (id,name,description, type, project_id) values ('9','Change','',1, 2); 
insert into category (id,name,description, type, project_id) values ('10','Failure-Software-Application','',1,1); 
insert into category (id,name,description, type, project_id) values ('11','Failure-Software-ERP', '',1, 1); 
insert into category (id,name,description, type, project_id) values ('12','Failure-Software-eMail', '',1, 1); 
insert into category (id,name,description, type, project_id) values ('13','Failure-Software-Internet', '',1, 1); 
insert into category (id,name,description, type, project_id) values ('14','Failure-Software-Other', '',1, 1); 
insert into category (id,name,description, type, project_id) values ('15','Failure-Hardware-Desktop', '',1, 1); 
insert into category (id,name,description, type, project_id) values ('16','Failure-Hardware-Printer', '',1, 1); 
insert into category (id,name,description, type, project_id) values ('17','Failure-Hardware-Connectivity', '',1, 1); 
insert into category (id,name,description, type, project_id) values ('18','Failure-Hardware-Other', '',1, 1); 
insert into category (id,name,description, type, project_id) values ('19','Service-HelpDesk-Password', '',1, 1); 
insert into category (id,name,description, type, project_id) values ('20','Service-HelpDesk-Connectivity', '',1, 1); 
insert into category (id,name,description, type, project_id) values ('21','Service-HelpDesk-Software', '',1, 1); 
insert into category (id,name,description, type, project_id) values ('22','Service-HelpDesk-Other', '',1, 1); 
insert into category (id,name,description, type, project_id) values ('23','Other','',1, NULL); 
insert into category (id,name,description, type, project_id) values ('24','Technical Risk','',4, NULL); 
insert into category (id,name,description, type, project_id) values ('25','Project Management Risk','',4, NULL); 
insert into category (id,name,description, type, project_id) values ('26','Organizational Risk','',4, NULL); 
insert into category (id,name,description, type, project_id) values ('27','External Risk','',4, NULL);
insert into category (id,name,description, type, project_id) values ('28','Project Management','', 2, NULL); 
insert into category (id,name,description, type, project_id) values ('29','Resource Management','', 2, NULL); 
insert into category (id,name,description, type, project_id) values ('30','Business Management','', 2, NULL); 
insert into category (id,name,description, type, project_id) values ('31','Management','', 3, NULL); 
insert into category (id,name,description, type, project_id) values ('32','Customer Relationship','', 3, NULL); 
insert into category (id,name,description, type, project_id) values ('33','Cost Center A','', 7, NULL);
insert into category (id,name,description, type, project_id) values ('34','Cost Center B','', 7, NULL);
insert into category (id,name,description, type, project_id) values ('35','Cost Center N','', 7, NULL); 
insert into category (id,name,description, type, project_id) values ('36','Specification','', 8, NULL);
insert into category (id,name,description, type, project_id) values ('37','Food','', 9, NULL);
insert into category (id,name,description, type, project_id) values ('39','Transport','', 9, NULL);
insert into category (id,name,description, type, project_id) values ('40','Aquisition','', 9, NULL);  

insert into customer (id,project_id) values ('2','0'); 
insert into customer (id,project_id) values ('3','0'); 
insert into customer (id,project_id) values ('4','0'); 
insert into customer (id,project_id) values ('5','0'); 
insert into customer (id,project_id) values ('2','1'); 
insert into customer (id,project_id) values ('4','1'); 
insert into customer (id,project_id) values ('5','1'); 
insert into customer (id,project_id) values ('2','2'); 
insert into customer (id,project_id) values ('4','2'); 
insert into customer (id,project_id) values ('5','2'); 

insert into resource (id,project_id,capacity_per_day) values ('3','0', 480); 
insert into resource (id,project_id,capacity_per_day) values ('5','1', 480); 
insert into resource (id,project_id,capacity_per_day) values ('2','2', 480); 
insert into resource (id,project_id,capacity_per_day) values ('4','2', 480); 

insert into customer (id,project_id) values ('3','1'); 
insert into customer (id,project_id) values ('3','2'); 
insert into resource (id,project_id,capacity_per_day) values ('3','1', 480); 
insert into resource (id,project_id,capacity_per_day) values ('3','2', 480); 


insert into leader (id,project_id) values ('3','0'); 
insert into leader (id,project_id) values ('5','1'); 
insert into leader (id,project_id) values ('2','2'); 

insert into risk_status (id, name, description, status_type) values ('1', 'Identified', 'Identified', '0'); 
insert into risk_status (id, name, description, status_type) values ('2', 'Materialized', 'Materialized', '1'); 
insert into risk_status (id, name, description, status_type) values ('3', 'Canceled', 'Canceled', '2'); 

insert into root (id,project_id) values ('3','0'); 

insert into p_sequence (id) values (200);

insert into report (id, name, type, report_perspective_id, sql_text, execution_hour, last_execution, project_Id, final_date, data_type, file_name, profile_view) values ('2', 'Project KPI Histogram', 0, '', 'select rr.report_id as report_id , r.name as name, cast(rr.value as SIGNED) as val, count(rr.value) as count from report_result rr, report r where rr.report_id = r.id and r.report_perspective_id <> 0 and r.data_type = 0 and rr.last_execution >= ?#Initial Date{}(2)# and rr.last_execution <= ?#Final Date{}(2)# and rr.value <> "0" group by report_id, val', 0, null, 0, null, 0, '#CLASS_PATH#/WEB-INF/classes/KPIHistogram.jasper', '3');
insert into report (id, name, type, report_perspective_id, sql_text, execution_hour, last_execution, project_Id, final_date, data_type, file_name, profile_view) values ('4', 'Requirement Overview', 0, '', 'select r.id, r.suggested_date, r.deadline_date, r.priority, r.reopening, rs.name as status_name, c.name as category_name, p.description, p.creation_date, pr.name as project_name from requeriment r, requeriment_status rs, category c, planning p, project pr where r.category_id=c.id and r.requeriment_status_id = rs.id and r.project_id = pr.id and r.id = p.id and r.id in (?#Requirement ID{ }(1)#)', 0, null, 0, null, 0, '#CLASS_PATH#/WEB-INF/classes/ReqOverview.jasper', '3');
insert into report (id, name, type, report_perspective_id, sql_text, execution_hour, last_execution, project_Id, final_date, data_type, file_name, profile_view) values ('7', 'My Tasks Report', 0, '', 'select u.username, t.name, u.name as fullname, p.id as project_id, p.name as project, c.name as category, sub.task_id, sub.bucket_date, sum(sub.alloc_time) as s from ( select rt.task_id, rt.resource_id, rt.project_id, rta.sequence, rta.alloc_time, rt.actual_date, ADDDATE(rt.actual_date, rta.sequence-1) as bucket_date from resource_task rt, resource_task_alloc rta where rt.task_id = rta.task_id and rt.resource_id = rta.resource_id and rt.project_id = rta.project_id and rta.alloc_time > 0 and rt.actual_date is not null ) as sub, tool_user u, task t, project p, category c where sub.resource_id = u.id and sub.task_id = t.id and sub.project_id = p.id and t.category_id = c.id and sub.bucket_date >= ?#Initial Date{}(2)# and sub.bucket_date <= ?#Final Date{}(2)# and u.id=?#USER_ID# group by p.name, u.username, p.id , u.name, sub.task_id, t.name, c.name, sub.bucket_date order by u.username, sub.bucket_date, p.name, c.name', 0, null, 0, null, 0, '#CLASS_PATH#/WEB-INF/classes/MyTaskReport.jasper', '3');
insert into report (id, name, type, report_perspective_id, sql_text, execution_hour, last_execution, project_Id, final_date, data_type, file_name, profile_view) values ('8', 'Survey Report', 0, '', 'select s.id, s.name, s.description, s.is_anonymous, s.project_id, s.creation_date, s.final_date, s.date_publishing, p.name as project_name, q.content, q.is_mandatory, q.type, q.id as question_id from survey s, project p, survey_question q where s.project_id=p.id and q.survey_id = s.id and s.id = ?#Survey{!select id, name from survey where project_id=?#PROJECT_ID#!}(1)# order by s.id, q.position, q.sub_title', 0, null, 0, null, 0, '#CLASS_PATH#/WEB-INF/classes/SurveyReport.jasper', '2');
insert into report (id, name, type, report_perspective_id, sql_text, execution_hour, last_execution, project_Id, final_date, data_type, file_name, profile_view) values ('9', 'Expense Report', 0, '', 'select e.id, t.name, pr.name as PROJECT_NAME, p.description, p.creation_date, c.id as COST_ID, c.name as COST_NAME, g.name as CATEGORY, c.account_code, ci.due_date, ci.value, at.username as approver, cs.name as STATUS, cs.state_machine_order from expense e, tool_user t, project pr, planning p, cost c, category g, cost_status cs, cost_installment ci LEFT OUTER JOIN tool_user at on (at.id = ci.approver) where e.user_id = t.id and e.project_id = pr.id and e.id = p.id and c.category_id = g.id and c.expense_id = e.id and c.id = ci.cost_id and ci.installment_num=1 and ci.cost_status_id = cs.id and e.id=?#Expense{!select id, id as lbl from expense where user_id in (select id from resource where project_id in (?#PROJECT_DESCENDANT#)) or user_id=?#USER_ID#!}(1)# order by e.id', 0, null, 0, null, 0, '#CLASS_PATH#/WEB-INF/classes/ExpenseReport.jasper', '3');
insert into report (id, name, type, report_perspective_id, sql_text, execution_hour, last_execution, project_Id, final_date, data_type, file_name, profile_view) values ('10', 'Accountability Report', 0, '', 'select p.id as project_id, p.name as project_name, sub.id, sub.name, sub.due_date, sub.item_name, sub.value, sub.is_inv from ( select i.project_id, i.id, i.name, i.due_date, ii.name as item_name, (ii.amount * ii.price) as value, (ii.type in (1, 2)) as is_inv from invoice i, invoice_item ii, invoice_status s where ii.invoice_id = i.id and invoice_status_id = s.id and s.state_machine_order=100 union select c.project_id, c.id, cat.name, i.due_date, c.name, i.value, 0  as is_inv from cost c, cost_installment i, category cat where cat.id = c.category_id and c.id = i.cost_id ) as sub, project p where sub.project_id = p.id and p.id in (?#PROJECT_DESCENDANT#) order by p.id, sub.due_date, sub.is_inv', 0, null, 0, null, 0, '#CLASS_PATH#/WEB-INF/classes/AccountabilityReport.jasper', '2');

insert into project_report (report_id, project_id) values ('2' ,'0');
insert into project_report (report_id, project_id) values ('4' ,'0');
insert into project_report (report_id, project_id) values ('7' ,'0');
insert into project_report (report_id, project_id) values ('8' ,'0');
insert into project_report (report_id, project_id) values ('9' ,'0');
insert into project_report (report_id, project_id) values ('10','0');

insert into cost_status (id, name, description, state_machine_order) values ('1', 'Waiting Approve', 'Waiting Approve', 1);
insert into cost_status (id, name, description, state_machine_order) values ('2', 'Budgeted', 'Budgeted', 10);
insert into cost_status (id, name, description, state_machine_order) values ('3', 'Paid', 'Paid', 100);
insert into cost_status (id, name, description, state_machine_order) values ('4', 'Canceled', 'Canceled', 101);


insert into resource_capacity
select distinct r.id, r.project_id, EXTRACT(YEAR FROM p.creation_date), 
      EXTRACT(MONTH FROM p.creation_date), EXTRACT(DAY FROM p.creation_date),
      r.capacity_per_day, (r.cost_per_hour * 100)
from resource as r, customer as c, planning p
where r.id = c.id and r.project_id = c.project_id
and c.project_id = p.id
and (c.is_disable is null or c.is_disable=0) 
and r.capacity_per_day is not null;

insert into artifact_template (id, name, description, category_id, body_html) values ('1', 'Blank Document', 'This is a template for a blank document.', '0', '<body></body>');
insert into artifact_template (id, name, description, category_id, body_html) values ('420', 'Use Case Template', 'This is a template for a Use Case suite. Each Use Case focuses on describing how to achieve a goal or a task into a project. This template was inspired at <a href="http://readyset.tigris.org/nonav/templates/frameset.html">ReadSET templates</a>', '36', '<body><h2>Use Cases</h2><h3>Project: <span style="background-color: #ffff00">PROJECT_NAME</span></h3><p><span style="background-color: #ffff00"><i>TODO: Note any aspects that are common to all use cases here. This helps keep the use cases themselves short. If any use case is an exception, note it''s specific value to replace or add to the default.</i></span></p><p><span style="background-color: #ffff00"><i>TIP: It is OK to simply list the names of a lot of use cases without actually writing the use case in detail. Document the most important use cases first and come back to less important ones later.</i></p></span></p><h3>Default Aspects of All Use Cases</h3><table border="1" cellpadding="0" ><tr><td ><p><b>Direct Actors:</b></p></td><td><p><span style="background-color: #ffff00">User: end-user in any role</span></p><p><span style="background-color: #ffff00">System: The system being built</span></p><p><span style="background-color: #ffff00">When actors are not listed, assume User is doing it.</span></p><p><span style="background-color: #ffff00">Items beginning with see indicate that System has presented a new screen.</span></p></td></tr><tr><td><p><b>Stakeholders:</b></p></td><td><p><span style="background-color: #ffff00">Project Owners and other members</span></p></td></tr><tr><td><p><b>Prereq:</b></p></td><td><p><span style="background-color: #ffff00">User is logged in</span></p></td></tr></table></p><h3><span style="background-color: #ffff00">UC-00: Configure the site</span></h3><table border="1" cellpadding="0" ><tr><td><p><b>Summary:</b></p></td><td><p><span style="background-color: #ffff00">The administrator navigates to the site  configuration page and uses it to change the behavior of the web application.  Specifically, the user-visible timezone is being set.</span></p></td></tr><tr><td><p><b>Priority:</b></p></td><td><p><span style="background-color: #ffff00">Essential</span></p></td></tr><tr><td><p><b>Use Frequency:</b></p></td><td><p><span style="background-color: #ffff00">Rarely</span></p></td></tr><tr><td ><p><b>Direct Actors:</b></p></td><td><p><span style="background-color: #ffff00">Admin: Web-site administrator</span></p></td></tr><tr><td><p><b>Main Success  Scenario:</b></p></td><td><ol><li><span style="background-color: #ffff00">visit SiteConfiguration page</span></li><li><span style="background-color: #ffff00">see site configuration options</span></li><li><span style="background-color: #ffff00">enter timezone abbreviation for date displays</span></li><li><span style="background-color: #ffff00">submit form</span></li><li><span style="background-color: #ffff00">confirm changes</span></li><li><span style="background-color: #ffff00">see SiteConfiguration page again, with updated values</span></li></ol></td></tr><tr><td><p><b>Alternative</b></p><p><b>Scenario Extensions:</b></p></td><td><ul><li><span style="background-color: #ffff00">If the timezone abbreviation is incorrect, an error message will be displayed and no changes will be made.</span></li></ul></td></tr><tr><td><p><b>Notes and Questions</b></p></td><td><ul><li><span style="background-color: #ffff00">How will administrators know the right timezone abbreviation? They would know it if they live in that timezone. Maybe we could provide a drop-down list of all choices, but each would need some explanation.</span></li></ul></td></tr></table><h3><span style="background-color: #ffff00">UC-nn: USE CASE NAME</span></h3><table border="1" cellpadding="0" ><tr><td><p><b>Summary:</b></p></td><td><p><span style="background-color: #ffff00">1-3 SENTENCES</span></p></td></tr><tr><td><p><b>Priority:</b></p></td><td><p><span style="background-color: #ffff00">Essential | Expected | Desired | Optional</span></p></td></tr><tr><td><p><b>Use Frequency:</b></p></td><td><p><span style="background-color: #ffff00">Always | Often | Sometimes | Rarely |  Once</span></p></td></tr><tr><td><p><b>Main Success  Scenario:</b></p></td><td><ol><li><span style="background-color: #ffff00">STEP</span></li><li><span style="background-color: #ffff00">STEP</span></li><li><span style="background-color: #ffff00">STEP</span></li></ol></td></tr><tr><td><p><b>Alternative</b></p><p><b>Scenario  Extensions:</b></p></td><td><ul type=disc><li><span style="background-color: #ffff00">If CONDITION, then ALTERNATIVE STEPS.</span></li><ul><li><span style="background-color: #ffff00">NOTES or DETAILS.</span></li></ul><li><span style="background-color: #ffff00">If CONDITION, then ALTERNATIVE STEPS.</span></li><ul><li><span style="background-color: #ffff00">NOTES or DETAILS.</span></li></ul></ul></td></tr><tr><td><p><b>Notes and Questions</b></p></td><td><ul><li><span style="background-color: #ffff00">NOTE</span></li><li><span style="background-color: #ffff00">QUESTION</span></li></ul></td></tr></table></p></div></body>');
insert into artifact_template (id, name, description, category_id, body_html) values ('421', 'Test Case Template', 'This is a template for a Test Case suite. A test case describes a set of conditions and their expected results. This template was inspired at <a href="http://readyset.tigris.org/nonav/templates/frameset.html">ReadSET templates</a>', '36', '<body><h2>Test Cases</h2><h3>Project: <span style="background-color: #ffff00">PROJECT_NAME</span></h3></p><h3><span style="background-color: #ffff00">login-1: Normal User Login</span></h3><table border="1" cellpadding="0"><tr><td><p><b>Purpose:</b></p></td><td><p><span style="background-color: #ffff00">Test that users can log in with the proper username or email address and their password.</span></p></td></tr><tr><td><p><b>Prereq:</b></p></td><td><p><span style="background-color: #ffff00">User is not already logged in.</span></p><p><span style="background-color: #ffff00">User testuser exists, and account is in  good standing.</span></p></td></tr><tr><td><p><b>Test Data:</b></p></td><td><p><span style="background-color: #ffff00">usernameOrEmail = {testuser, bogususer, testuser@nospam.com, test@user@nospam.com, empty}</span></p><p><span style="background-color: #ffff00">password = {valid, invalid, empty}</span></p></td></tr><tr><td><p><b>Steps:</b></p></td><td><ol><li><span style="background-color: #ffff00">visit LoginPage</span></li><li><span style="background-color: #ffff00">enter usernameOrEmail</span></li><li><span style="background-color: #ffff00">enter password</span></li><li><span style="background-color: #ffff00">click Login</span></li><li><span style="background-color: #ffff00">see the terms-of-use page</span></li><li><span style="background-color: #ffff00">click Agree at page bottom</span></li><li><span style="background-color: #ffff00">click Submit</span></li><li><span style="background-color: #ffff00">see PersonalPage</span></li><li><span style="background-color: #ffff00">verify welcome message is correct username</span></li></ol></td></tr><tr><td><p><b>Notes and Questions:</b></p></td><td><ul><li><span style="background-color: #ffff00">This assumes that user has not agreed to terms-of-use already.</span></li><li><span style="background-color: #ffff00">Does this work without browser cookies?</span></li></ul></td></tr></table></p><h3><span style="background-color: #ffff00">login-2: Locked-out User Login</span></h3><table border="1" cellpadding="0"><tr><td><p><b>Purpose:</b></p></td><td><p><span style="background-color: #ffff00">Test that a user who has been locked out by a moderator, cannot log in, They should see a message indicating that they were locked out.</span></p></td></tr><tr><td><p><b>Prereq:</b></p></td><td><p><span style="background-color: #ffff00">User is not already logged in.</span></p><p><span style="background-color: #ffff00">User testuser2 exists, and has been locked out</span></p></td></tr><tr><td><p><b>Test Data:</b></p></td><td><p><span style="background-color: #ffff00">usernameOrEmail = {testuser2, testuser2@nospam.com}</span></p><p><span style="background-color: #ffff00">password = {valid}</span></p></td></tr><tr><td><p><b>Steps:</b></p></td><td><ol><li><span style="background-color: #ffff00">visit LoginPage</span></li><li><span style="background-color: #ffff00">enter usernameOrEmail</span></li><li><span style="background-color: #ffff00">enter password</span></li><li><span style="background-color: #ffff00">click Login</span></li><li><span style="background-color: #ffff00">see LoginPage</span></li><li><span style="background-color: #ffff00">verify warning message is the locked-out message</span></li></ol></td></tr><tr><td><p><b>Notes and Questions:</b></p></td><td><ul><li><span style="background-color: #ffff00">Does this work without browser cookies?</span></li></ul></td></tr></table></p><h3><span style="background-color: #ffff00">unique-test-case-id1: Test Case Title</span></h3><table border="1" cellpadding="0"><tr><td><p><b>Purpose:</b></p></td><td><p> </p></td></tr><tr><td><p><b>Prereq:</b></p></td><td><p> </p></td></tr><tr><td><p><b>Test Data:</b></p></td><td><p><span style="background-color: #ffff00">VAR = {VALUES}</span></p><p><span style="background-color: #ffff00">VAR = {VALUES}</span></p></td></tr><tr><td><p><b>Steps:</b></p></td><td><ol><li><span style="background-color: #ffff00">STEP</span></li><li><span style="background-color: #ffff00">STEP</span></li><li><span style="background-color: #ffff00">STEP</span></li><li><span style="background-color: #ffff00">verify ...</span></li></ol></td></tr><tr><td><p><b>Notes and Questions:</b></p></td><td><ul><li><span style="background-color: #ffff00">NOTE</span></li><li><span style="background-color: #ffff00">QUESTION</span></li></ul></td></tr></table></p><h3><span style="background-color: #ffff00">unique-test-case-id2: Test Case Title</span></h3><table border="1" cellpadding="0"><tr><td><p><b>Purpose:</b></p></td><td><p> </p></td></tr><tr><td><p><b>Prereq:</b></p></td><td><p> </p></td></tr><tr><td><p><b>Test Data:</b></p></td><td><p><span style="background-color: #ffff00">VAR = {VALUES}</span></p><p><span style="background-color: #ffff00">VAR = {VALUES}</span></p></td></tr><tr><td><p><b>Steps:</b></p></td><td><ol><li><span style="background-color: #ffff00">STEP</span></li><li><span style="background-color: #ffff00">STEP</span></li><li><span style="background-color: #ffff00">STEP</span></li><li><span style="background-color: #ffff00">verify ...</span></li></ol></td></tr><tr><td><p><b>Notes and Questions:</b></p></td><td><ul><li><span style="background-color: #ffff00">NOTE</span></li><li><span style="background-color: #ffff00">QUESTION</span></li></ul></td></tr></table></p></body>');
insert into artifact_template (id, name, description, category_id, body_html) values ('422', 'Software Requirements Specification', 'This is a template for a SRS. A SRS contain a understanding of a customer requirements and dependencies. This template was inspired at <a href="http://readyset.tigris.org/nonav/templates/frameset.html">ReadSET templates</a>', '36', '<body><h2>Software Requirements Specification</h2><B>Project: <span style="background-color: #ffff00">PROJECT_NAME</span></B></p><h3>Introduction</h3><p><i><span style="background-color: #ffff00">TODO: Provide a brief overview of thisrelease of the product. You can copy text from the project proposal, paste ithere, and shorten it.</span></i></p><p><span style="background-color: #ffff00">PARAGRAPH</span></p><p><span style="background-color: #ffff00">PARAGRAPH</span></p></p><h3>Use Cases</h3><p><span style="background-color: #ffff00">ONE PARAGRAPH OVERVIEW</span></p><ul> <li><span style="background-color: #ffff00">The use case suite lists all use cases in an organized way.</span></li></ul></p><h3>Functional Requirements</h3><p><span style="background-color: #ffff00">ONE PARAGRAPH OVERVIEW</span></p></p><h3>Non-Functional Requirements</h3><p><i><span style="background-color: #ffff00">TODO: Describe the non-functional requirements for this release. Some examples are provided below.</span></i></p></p><p><b>What are the usability requirements?</b></p><p><span style="background-color: #ffff00">Our main criteria for making the system usable is the difficulty of performing each high-frequency use case. Difficultydepends on the number of steps, the knowledge that the user must have at each step, the decisions that the user must make at each step, and the mechanics ofeach step (e.g., typing a book title exactly is hard, clicking on a title in a list is easy).</span></p><p><span style="background-color: #ffff00">The user interface should be as familiar as possible to users who have used other web applications and Windows desktop applications. E.g., we will follow the UI guidelines for naming menus,buttons, and dialog boxes whenever possible.</span></p><p><span style="background-color: #ffff00">PARAGRAPH</span></p><p><span style="background-color: #ffff00">Details:</span></p><li><span style="background-color: #ffff00">Government compliance</span></p><li><span style="background-color: #ffff00">The customer wants extensive on-line help, but is not demanding a printed manual.</span></p><p><b>What are the reliability and up-timerequirements?</b></p><p><span style="background-color: #ffff00">PARAGRAPH</span></p><p><span style="background-color: #ffff00">PARAGRAPH</span></p><p><span style="background-color: #ffff00">Details:</span></p><li><span style="background-color: #ffff00">DETAIL</span></li><li><span style="background-color: #ffff00">DETAIL</span></li><li><span style="background-color: #ffff00">DETAIL</span></li><p><b>What are the safety requirements?</b></p><p><span style="background-color: #ffff00">PARAGRAPH</span></p><p><span style="background-color: #ffff00">PARAGRAPH</span></p><p><span style="background-color: #ffff00">Details:</span></p><li><span style="background-color: #ffff00">DETAIL</span></li><li><span style="background-color: #ffff00">DETAIL</span></li><li><span style="background-color: #ffff00">DETAIL</span></li><p><b>What are the security requirements?</b></p><p><span style="background-color: #ffff00">Access will be controlled with usernames and passwords.</span></p><p><span style="background-color: #ffff00">Only administrator users will have access to administrative functions, average users will not.</span></p><p><span style="background-color: #ffff00">Details:</span></p><li><span style="background-color: #ffff00">Passwords must be 4-14 characters long</span></li><li><span style="background-color: #ffff00">We will not use encrypted communications (SSL) for this website</span></li><li><span style="background-color: #ffff00">DETAIL</span></li><p><b>What are the performance and scalability requirements requirements?</b></p><p><span style="background-color: #ffff00">PARAGRAPH</span></p><p><span style="background-color: #ffff00">PARAGRAPH</span></p><p><span style="background-color: #ffff00">Details:</span></p><li><span style="background-color: #ffff00">DETAIL</span></li><li><span style="background-color: #ffff00">DETAIL</span></li><li><span style="background-color: #ffff00">DETAIL</span></li><p><b>What are the maintainability and upgradability requirements?</b></p><p><span style="background-color: #ffff00">Maintainability is our ability to make changes to the product over time. We need strong maintainability in order to retain our early customers. We will address this by anticipating several types of change, and by carefully documenting our design andimplementation.</span></p><p><span style="background-color: #ffff00">Upgradability is our ability to cost-effectively deploy new versions of the product to customers with minimal downtime or disruption. A key feature supporting this goal is automaticdownload of patches and upgrade of the end-user machine. Also, we shall use data file formats that include enough meta-data to allow us to reliably transform existing customer data during an upgrade.</span></p><p><span style="background-color: #ffff00">Details:</span></p><li><span style="background-color: #ffff00">DETAIL</span></li><li><span style="background-color: #ffff00">DETAIL</span></li><li><span style="background-color: #ffff00">DETAIL</span></li><p><b>What are the supportability and operability requirements?</b></p><p><span style="background-color: #ffff00">Supportability is our ability to provide cost effective technical support. Our goal is to limit our support costs to only 5% of annual licensing fees. The products automatic upgrade feature will help us easily deploy defect fixes to end-users. The user guideand product website will include a troubleshooting guide and checklist of information to have at hand before contacting technical support.</span></p><p><span style="background-color: #ffff00">Operability is our ability to host and operate the software as an ASP (Application Service Provider). The product features should help us achieve our goal of 99.9% uptime (at most 43minutes downtime each month). Key features supporting that are the ability to do hot data backups, and application monitoring.</span></p><p><span style="background-color: #ffff00">Details:</span></p><li><span style="background-color: #ffff00">DETAIL</span></li><li><span style="background-color: #ffff00">DETAIL</span></li><li><span style="background-color: #ffff00">DETAIL</span></li><p><b>What are the business life-cycle requirements?</b></p><p><span style="background-color: #ffff00">The business life-cycle of a product includes everything that happens to that product over a period of several years, from initial purchase decision, through important but infrequentuse cases, until product retirement. Key life-cycle requirements are listed below.</span></p><p><span style="background-color: #ffff00">Details:</span></p><li><span style="background-color: #ffff00">Customers must be able to manage the number of licenses that they have and make informed decisions to purchase more licenses when needed</span></li><li><span style="background-color: #ffff00">The product shall support daily operations and our year-end audit</span></li><li><span style="background-color: #ffff00">The customer data shall be stored in a format that is still accessible even after the application has been retired</span></li></p><h3>Environmental Requirements</h3><p><i><span style="background-color: #ffff00">TODO: Describe the environmental requirements for this release. Environmental requirements describe the larger system of hardware, software, and data that this product must work within. Someexamples are provided below.</span></i></p><p><b>What are the system hardware requirements?</b></p><p><span style="background-color: #ffff00">PARAGRAPH</span></p><p><span style="background-color: #ffff00">PARAGRAPH</span></p><p><span style="background-color: #ffff00">Details:</span></p><li><span style="background-color: #ffff00">DETAIL</span></li><li><span style="background-color: #ffff00">DETAIL</span></li><li><span style="background-color: #ffff00">DETAIL</span></li><p><b>What are the system software requirements?</b></p><p><span style="background-color: #ffff00">PARAGRAPH</span></p><p><span style="background-color: #ffff00">PARAGRAPH</span></p><p><span style="background-color: #ffff00">Details:</span></p><li><span style="background-color: #ffff00">DETAIL</span></li><li><span style="background-color: #ffff00">DETAIL</span></li><li><span style="background-color: #ffff00">DETAIL</span></li><p><b>What application program interfaces (APIs) must be provided?</b></p><p><span style="background-color: #ffff00">PARAGRAPH</span></p><p><span style="background-color: #ffff00">PARAGRAPH</span></p><p><span style="background-color: #ffff00">Details:</span></p><li><span style="background-color: #ffff00">We must implement this standard API</span></li><li><span style="background-color: #ffff00">DETAIL</span></li><li><span style="background-color: #ffff00">DETAIL</span></li><p><b>What are the data import and export requirements?</b></p><p><span style="background-color: #ffff00">PARAGRAPH</span></p><p><span style="background-color: #ffff00">PARAGRAPH</span></p><p><span style="background-color: #ffff00">Details:</span></p><li><span style="background-color: #ffff00">The system will store all data in a standard SQL database, where it can be accessed by other programs.</span></li><li><span style="background-color: #ffff00">The system will store all data in an XML file, using a standard DTD.</span></li><li><span style="background-color: #ffff00">The system will read and write valid .XYZ files used by OTHER APPLICATION</span></li><li><span style="background-color: #ffff00">DETAIL</span></li></body>');
update artifact_template set header_html='<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN\" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"><html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8" /><style type="text/css">body, td, pre {color:#000; font-family:Verdana, Arial, Helvetica, sans-serif; font-size:10px; margin:8px;} body {background:#FFF;} body.mceForceColors {background:#FFF; color:#000;} h1 {font-size: 2em} h2 {font-size: 1.5em} h3 {font-size: 1.17em} h4 {font-size: 1em} h5 {font-size: .83em} h6 {font-size: .75em} .mceItemTable, .mceItemTable td, .mceItemTable th, .mceItemTable caption, .mceItemVisualAid {border: 1px dashed #BBB;} a.mceItemAnchor {display:inline-block; width:11px !important; height:11px  !important; background:url(img/items.gif) no-repeat 0 0;} td.mceSelected, th.mceSelected {background-color:#3399ff !important} img {border:0;} table {cursor:default} table td, table th {cursor:text} ins {border-bottom:1px solid green; text-decoration: none; color:green} del {color:black; text-decoration:line-through} cite {border-bottom:1px dashed blue} acronym {border-bottom:1px dotted #CCC; cursor:help} abbr {border-bottom:1px dashed #CCC; cursor:help} span.bold {font-weight: bold;} span.italic {font-style:italic;} span.underline{text-decoration: underline} p.right {text-align:right;} p.left {text-align:left;} p.center {text-align:center;} p.full {text-align:justify;} * html body {scrollbar-3dlight-color:#F0F0EE;scrollbar-arrow-color:#676662;scrollbar-base-color:#F0F0EE;scrollbar-darkshadow-color:#DDD;scrollbar-face-color:#E0E0DD;scrollbar-highlight-color:#F0F0EE;scrollbar-shadow-color:#F0F0EE;scrollbar-track-color:#F5F5F5;} img:-moz-broken {-moz-force-broken-image-icon:1; width:24px; height:24px} font[face=mceinline] {font-family:inherit !important}</style>';
update artifact_template set footer_html='</html>';

insert into planning (id, description, creation_date, final_date, iteration, rich_text_desc) values('190', null, '2012-10-25', null, null, null);
insert into template (id, name, deprecated_date, root_node_id, category_id) values ('190', 'Bug Treatment reported by the Customer', null, '90', '0');
insert into node_template (id, name, description, node_type, planning_id, project_id, next_node_id) values ('90', 'Bug Evaluation', 'Analise, reproduce and evaluate the bug occurrence that was reported by customer.', '1', '190', null, '91'); 
insert into node_template (id, name, description, node_type, planning_id, project_id, next_node_id) values ('91', 'Bug Verification', 'Verify the occurrence (after evaluation) to ensure that is really a bug.', '2', '190', null, '92'); 
insert into node_template (id, name, description, node_type, planning_id, project_id, next_node_id) values ('92', 'Fixing the feature (...)', '[write here any suggestions or attention points to be considered by the analyst during the feature rework]', '1', '190', null, '93'); 
insert into node_template (id, name, description, node_type, planning_id, project_id, next_node_id) values ('93', 'Testing the feature (...)', '[write here the scope of the test to be performed or information that helps the analyst during validation testing of functionality]', '1', '190', null, '94'); 
insert into node_template (id, name, description, node_type, planning_id, project_id, next_node_id) values ('94', 'Feature validating', 'Verify whether the test has passed', '2', '190', null, null); 
insert into step_node_template (id, category_id, resource_list, category_regex) values ('90', '0', null, null);
insert into decision_node_template (id, question_content, next_node_id_if_false) values ('91', 'Is this a bug?', null);
insert into step_node_template (id, category_id, resource_list, category_regex) values ('92', '0', null, null);
insert into step_node_template (id, category_id, resource_list, category_regex) values ('93', '0', null, null);
insert into decision_node_template (id, question_content, next_node_id_if_false) values ('94', 'The test passed?', '92');

/*
--insert into planning (id, description, creation_date, final_date, iteration, rich_text_desc) values('191', null, '2013-01-10', null, null, null);
--insert into template (id, name, deprecated_date, root_node_id, category_id) values ('191', 'Test Ad-Hoc', null, '100', '0');
--insert into node_template (id, name, description, node_type, planning_id, project_id, next_node_id) values ('100', 'Test feature (...)', '[write here the scope of the test to be performed or information that helps the analyst during validation testing of functionality]', '1', '191', null, '101');
--insert into node_template (id, name, description, node_type, planning_id, project_id, next_node_id) values ('101', 'Feature validating', 'Verify whether the test has passed', '2', '191', null, null);
--insert into node_template (id, name, description, node_type, planning_id, project_id, next_node_id) values ('102', 'Fixing the feature (...)', '[write here any suggestions or attention points to be considered by the analyst during the feature rework]', '1', '191', null, '100'); 
--insert into step_node_template (id, category_id, resource_list, category_regex)  values ('100', '0', null, null);
--insert into step_node_template (id, category_id, resource_list, category_regex)  values ('102', '0', null, null);
--insert into decision_node_template (id, question_content, next_node_id_if_false) values ('101', 'The test passed?', '102');
*/
