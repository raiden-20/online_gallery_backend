<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "form">
    <a class="toLogin" href="${url.loginUrl}"> 
        < <span>Назад</span>
    </a>
        <p class="instruction">${msg("emailVerifyInstruction1",user.email)}</p> 
    </#if>
</@layout.registrationLayout>