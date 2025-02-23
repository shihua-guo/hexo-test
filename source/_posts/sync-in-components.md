---
title: 【vue】2个组件之间实现同步Vue
date: 2017-11-27 23:05:40
categories: 前端
tags: vue
---
需求：**组件1**为**组件2**的子组件，**组件1**的A方法需要与**组件2**的B方法同步。
使用**$emit**进行通知。在**组件1**中调用**save**方法前，需要接受**组件2**的**beforeSave**方法传回的参数。
可以在**组件1**中使用创建**beforeSave**通知组件2执行**beforeSave**方法，然后当**组件2**执行完**beforeSave**之后回调，再使用$emit通知子组件执行**save**方法
####组件2中的组件1，
```
    <component :ref="id" @beforeSave="beforeSave"> <component>
    methods:{
        beforeSave({entity,formName,isClear}){
            axios.get('/api/getForeignKey').then(function(resp){
                let foreignKey = resp.data.foreignKey
                this.$refs['id'].$emit('save',foreignKey);
            }).catch(function(error){
                console.log(error)
            })
        }
    }
```
####组件1通知组件2
```
    beforeSave(entity,formName,isClear){
        this.$emit('beforeSave',{entity,formName,isClear})
    },
    save(foreignKey){
        console.log(foreignKey)
    }
```
