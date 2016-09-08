package com.cheng.weixin.service.user.service;

import com.cheng.weixin.rpc.user.entity.Account;
import com.cheng.weixin.rpc.user.entity.AccountLevel;
import com.cheng.weixin.rpc.user.entity.Member;
import com.cheng.weixin.rpc.user.enumType.Credit;
import com.cheng.weixin.rpc.user.enumType.Sex;
import com.cheng.weixin.rpc.user.enumType.SourceFrom;
import com.cheng.weixin.rpc.user.service.RpcUserService;
import com.cheng.weixin.service.user.dao.AccountDaoMapper;
import com.cheng.weixin.service.user.dao.AccountLevelDaoMapper;
import com.cheng.weixin.service.user.dao.MemberDaoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Desc: 用户
 * Author: cheng
 * Date: 2016/7/7
 */
@Service("userService")
public class UserService implements RpcUserService {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AccountDaoMapper accountDao;
    @Autowired
    private AccountLevelDaoMapper accountLevelDao;
    @Autowired
    private MemberDaoMapper memberDao;

    @Override
    public Account getAccountByLoginName(String loginName) {
        return accountDao.load(new Account(loginName));
    }

    @Override
    public void saveAccess(String phone, String password, String nickname, String ip) {

        if (null != getAccountByLoginName(phone)) {
            logger.info("当前用户{}已存在", phone);
            throw new IllegalArgumentException("当前用户"+phone+"已存在");
        }

        AccountLevel accountLevel = accountLevelDao.load(new AccountLevel(true));

        Account account = new Account();
        account.setUsername(phone);
        account.setPassword(password);
        account.setAccountLevelId(accountLevel.getId());
        account.setBalance(new BigDecimal(0));
        account.setBonusPointUpgrade(accountLevel.getPointEveryLevel());
        account.setBonusPointUsable(0);
        account.setCredit(Credit.WELL);
        account.setEmployee(false);
        account.setIp(ip);
        account.setSourceFrom(SourceFrom.NO);
        account.preInsert();
        accountDao.save(account);

        Member member = new Member();
        member.setAccountId(account.getId());
        member.setNickname(nickname);
        member.setMobile(phone);
        member.setSex(Sex.NO);
        member.preInsert();
        memberDao.save(member);
    }

    @Override
    public String validateLogin(String loginName, String password, String loginIp) {
        Account userAccount = accountDao.load(new Account(loginName));
        if (null != userAccount) {
            if (!password.equals(userAccount.getPassword())) {
                return "PASSWDFAIL";
            }
        }else {
            return "NOTUSER";
        }
        // 登陆成功保存登陆者的IP
        userAccount.setIp(loginIp);
        userAccount.preUpdate();
        accountDao.update(userAccount);
        return "SUCCESS";
    }
}