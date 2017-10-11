package com.centerm.jnbank.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by linwenhui on 2016/10/27.
 */
@DatabaseTable(tableName = "tb_employee")
public class Employee {
    @DatabaseField(defaultValue = "01", id = true)
    private String code;
    @DatabaseField
    private String password;

    public Employee() {
    }

    public Employee(String code, String password) {
        this.code = code;
        this.password = password;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object obj) {
        Employee employee = (Employee) obj;
        return this.code.equals(employee.code) && this.password.equals(employee.password);
    }
}
