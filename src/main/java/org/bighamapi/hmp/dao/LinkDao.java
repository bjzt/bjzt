package org.bighamapi.hmp.dao;

import org.bighamapi.hmp.pojo.Link;
import org.bighamapi.hmp.pojo.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface LinkDao extends JpaRepository<Link,String> {

}
