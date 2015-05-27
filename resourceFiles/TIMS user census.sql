CREATE TABLE #temp
(
	account_name varchar(50),
	jr1 varchar(1),
	jr2 varchar(1),
	ww varchar(1),
	ch varchar(1),
	hh varchar(1)
)

-- populate a temp table that for all TIMS usernames the theatre sites that they can access TIMS from
-- flattens many rows to a matrix
INSERT INTO #temp (account_name, jr1, jr2, ww, ch, hh)
select account_name,
max(case location_group_id when 2 then 'Y' else 'N' end),  -- JR1 theatre
max(case location_group_id when 1 then 'Y' else 'N' end), -- JR2 theatre
max(case location_group_id when 17 then 'Y' when 18 then 'Y' when 19 then 'Y' else 'N' end), -- WW theatre
max(case location_group_id when 9 then 'Y' else 'N' end), -- CH theatre
max(case location_group_id when 10 then 'Y' else 'N' end) -- HH theatre
from tims.account_location_privilages
group by account_name


select	s.staff_id AS TIMS_ID,
		--s.title + ' ' + s.forename + ' ' + upper(s.surname) AS NAME,
		s.title AS TITLE,
		s.forename AS FORENAME,
		s.surname AS SURNAME,
		p.name AS PROFESSION,
		--iif (s.archive_flag = '01-JAN-1900', 'Y', 'N') as "CURRENTLY ACTIVE" --SQL SERVER 2012 statement
		case when s.archive_flag = '01-JAN-1900' then 'Y'
		else	'N' 
		end AS CURRENTLY_ACTIVE,
		case when ssu.account_name is not null then ssu.account_name
		else ''
		end as HAS_ACCOUNT,
		case when count(stg.staff_id) > 0 then 'Y'
		else 'N'
		end as APPEAR_IN_TIMS_DROPDOWN,
		coalesce(tmp.jr1, '') as JR1,
		coalesce(tmp.jr2, '') as JR2,
		coalesce(tmp.ww, '') as WW,
		coalesce(tmp.ch, '') as CH,
		coalesce(tmp.hh, '') as HH
		
from tims.staff s
inner join tims.professions p
  on s.profession_code = p.profession_code
left outer join tims.staff_sysusers ssu
  on s.staff_id = ssu.staff_id
left outer join tims.staff_theatre_groups stg
  on s.staff_id = stg.staff_id
left outer join tims.account_activity act
  on ssu.account_name = act.account_name
left outer join #temp tmp
   on ssu.account_name = tmp.account_name
where s.profession_code not in ('PSEUD') -- do not include Pseudo-profession
group by s.staff_id, s.title, s.forename, s.surname, p.name, s.archive_flag, ssu.account_name, stg.staff_id, tmp.jr1, tmp.jr2, tmp.ww, tmp.ch, tmp.hh, act.last_login
order by surname asc

drop table #temp



