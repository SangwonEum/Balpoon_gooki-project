package controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import models.CLOBDao;
import models.alterDao;
import models.commentDao;
import models.massageDao;
import models.mongoDao;
import models.roomDao;
import models.zzimlistDao;
import utils.APIKeys;
import utils.Urlpicture;

@Controller
public class detail_controller {

	@Autowired
	roomDao rd;
	@Autowired
	Urlpicture urp;
	@Autowired
	alterDao aDao;
	@Autowired
	CLOBDao tDao;
	@Autowired
	APIKeys apiKey;
	@Autowired
	mongoDao mDao;
	
	// 댓글
	@Autowired
	commentDao cdao;

	// 찜
	@Autowired
	zzimlistDao zdao;
	
	@Autowired
	massageDao msgdao;

	@RequestMapping("/detail")
	public ModelAndView detailViewHandler(@RequestParam Map n, HttpServletRequest req, HttpServletResponse resp,
			HttpSession session) {

		// �궗吏꾩쿂由�
		JSONArray picturesJ = null;
		int num = Integer.parseInt(n.get("num") + "");

		Cookie c = new Cookie(String.valueOf(n.get("num")), String.valueOf(n.get("num")));
		c.setMaxAge(60 * 60);
		resp.addCookie(c);

		String imagelist = mDao.AllImage(String.valueOf(num));
		System.out.println(imagelist.toString());
		List<String> all = urp.get_picture_urls("https://www.zigbang.com/items1/" + num);
		picturesJ = new JSONArray(all);

		ModelAndView mav = new ModelAndView();
		Map map = new HashMap<>();
		map = rd.getSelectedRoomInfo(num);
		String aterid = map.get("ID").toString();
		
		Map map1 = new HashMap<>();
		map1.put("alterid", aterid);

		map1.put("sell_num", num);
		Map map2 = new HashMap<>();
		map2 = tDao.test(map1);
		Map agentmap = new HashMap<>();
		Map personmap = new HashMap<>();
		agentmap = aDao.agentInfo(map1);
		personmap = aDao.personInfo(map1);
		System.out.println(agentmap+"!!!!!!!!!!");
		System.out.println(personmap+"!!!!!!!!!!");
		if (agentmap != null) {

			String REGNAME = agentmap.get("BK_REGNAME").toString();
			
			if (REGNAME.contains("(")&&REGNAME.contains(")")) {
				String BK_REGNAME = REGNAME.substring(agentmap.get("BK_REGNAME").toString().indexOf("(") + 1,
						agentmap.get("BK_REGNAME").toString().indexOf(")"));

				agentmap.put("BK_REGNAME", BK_REGNAME);
			}else{
				agentmap.put("BK_REGNAME", REGNAME);
			}
			
			String BK_OFFICENAME = agentmap.get("BK_OFFICENAME").toString();
			if (BK_OFFICENAME.contains("(")&& BK_OFFICENAME.contains(")")) {
				BK_OFFICENAME = agentmap.get("BK_OFFICENAME").toString().substring(
						agentmap.get("BK_OFFICENAME").toString().indexOf("(") + 1,
						agentmap.get("BK_OFFICENAME").toString().indexOf(")"));
			}
			agentmap.put("reg", BK_OFFICENAME);
			
			
			// ===============================================
			
			
			
			
			
			
			int zr = 0;
			Map zmap = new HashMap();
			//zmap.put("sessionid", email);
			//String email = (String) session.getAttribute("email");
			String email = "";
			if(session.getAttribute("email")!= null){
				email = (String)session.getAttribute("email");
				zmap.put("sessionid", email);
			}else if(session.getAttribute("brokerid")!=null){
				email = (String)session.getAttribute("brokerid");
				zmap.put("sessionid", email);
			}
			
			zmap.put("num", num);
			zr = zdao.zzimActivation(zmap);
			System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx   " + zr);
			if (zr == 1) {
				mav.addObject("zzim", zr);
				mav.addObject("list", agentmap);
				mav.setViewName("t_detail");
			} else {
				mav.addObject("zzim", zr);
				mav.addObject("list", agentmap);
				mav.setViewName("t_detail");
			}

		} else {
			// ===============================================
			int zr = 0;
			
			String email = (String) session.getAttribute("email");
			Map zmap = new HashMap();
			zmap.put("sessionid", email);
			zmap.put("num", num);
			zr = zdao.zzimActivation(zmap);
			System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx   " + zr);
			if (zr == 1) {
				mav.addObject("zzim", zr);
				mav.addObject("list", personmap);
				mav.setViewName("t_detail1");
			} else {
				mav.addObject("zzim", zr);
				mav.addObject("list", personmap);
				mav.setViewName("t_detail1");
			}
		
		}
		mav.addObject("list2", map2);
		mav.addObject("list1", map);
		mav.addObject("pj", all);
		mav.addObject("apiKey", apiKey.getGOOGLE_MAP_KEY());
		mav.addObject("imglist", imagelist);

		// =========================================

		int cnt = cdao.commentcount(num);
		System.out.println("cnt : " + cnt);
		int size = cnt % 4 == 0 ? cnt / 4 : cnt / 4 + 1;
		String pStr = req.getParameter("page") == null ? "1" : req.getParameter("page");
		int start = (Integer.parseInt(pStr) - 1) * 4 + 1;
		int end = Integer.parseInt(pStr) * 4;
		// System.out.println("num==============> "+num);

		System.out.println("pStr : " + pStr);
		mav.addObject("size", size);

		mav.addObject("page", pStr);
		mav.addObject("num", num);
		// mav.addObject("num",num);

		List Clist = cdao.paging(start, end, num);
		System.out.println("由ъ뒪�듃 �궗�씠利� : " + Clist.size());
		mav.addObject("Clist", Clist);

	

		return mav;
	}
	
	@RequestMapping("/getApiInfo")
	@ResponseBody
	public List<HashMap> getApiInfoHandler(
			@RequestParam HashMap<String, Object> bounds) {
		
		System.out.println("Detail Map Bounds : " + bounds);
		List<HashMap> hospital = new ArrayList<>();
		hospital = mDao.getHospitalInfo(bounds);
		
		return hospital;
	}
}