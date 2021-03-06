package com.kenta.tabuchi;


import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class ViewController {
	
    @Autowired
    private JdbcTemplate jdbc;
    
	
	@RequestMapping(value= {"/","/{order}"},method=RequestMethod.GET)
	public ModelAndView indexGet(@RequestParam(name="order",required=false)Integer order,ModelAndView mav) {
		mav.setViewName("index");
		M_StudentDao dao = new M_StudentDao(jdbc);
		List<Student> recordset = null;
		if(order==null) {
			recordset= dao.findAll();
		}
		else {
			switch(order) {
			case 0:recordset = dao.findAllByOrderByNamePhonetic();	break;
			case 1:recordset = dao.findAllByOrderById(); 			break;
			case 2:recordset = dao.findAllByOrderByBirthday();		break;
			}
		}
		mav.addObject("order",order);
		mav.addObject("recordSet", recordset);
		return mav;
	}
	/**
	 * When user push button for find by name.This method will invoke.
	 * @param mav
	 * @return
	 */
	@RequestMapping(value="/",params="onCsvImportClick",method=RequestMethod.POST)
	public ModelAndView indexPostCsv(
			@RequestParam("upload_file")MultipartFile uploadFile,
			ModelAndView mav)
	{
		CsvReader csvReader = new CsvReader();
		M_StudentDao dao = new M_StudentDao(jdbc);
		csvReader.addTableFromCsv(uploadFile,dao);
		try {
			Thread.sleep(3000);//if here is not this,caused MySQL error.communication link failer.
		}catch(InterruptedException e) {e.printStackTrace();}
		mav = new ModelAndView("redirect:/");
		return mav;
	}
	/**
	 * When user push add_record button,Active page will move there.and this method
	 * will invoke like a constructor.
	 * @param student
	 * @param mav
	 * @return
	 */
	@RequestMapping(value="/add_record",method=RequestMethod.GET)
	public ModelAndView addRecord(@ModelAttribute("formModel")Student student,ModelAndView mav) {
		mav.setViewName("add_record");
		return mav;
	}
	
	/**
	 * This method implements validation to add_record form.
	 * @param student
	 * @param result
	 * @param mav
	 * @return
	 */
	@RequestMapping(value="/add_record",method=RequestMethod.POST)
	public ModelAndView form(
			@ModelAttribute("formModel") @Validated Student student,
			BindingResult result,
			ModelAndView mav)
	{
		ModelAndView adoptedMav = null;
		if(!result.hasErrors()) {
			M_StudentDao dao = new M_StudentDao(jdbc);
			dao.insert(student);
			try {
				Thread.sleep(3000);//if here is not this,caused MySQL error.communication link failer.
			}catch(InterruptedException e) {e.printStackTrace();}
			adoptedMav = new ModelAndView("redirect:/");
		}else {
			mav.setViewName("add_record");
			adoptedMav = mav;
		}
		return adoptedMav;
	}

	@RequestMapping(value= {"/find_record","/find_record/{radioValue}"},method=RequestMethod.GET)
	public ModelAndView findRecordGet(
			@RequestParam(name="radioValue",required=false)Integer radioValue,
			@RequestParam(name="textValue",required=false)String textValue,
			ModelAndView mav) {
		mav.setViewName("find_record");
		M_StudentDao dao = new M_StudentDao(jdbc);
		List<Student> recordset = null;
		if(radioValue==null) {
			recordset= dao.findAll();
		}
		else {
			switch(radioValue) {
			case 0:recordset = dao.findByNameLike(textValue);		break;
			case 1:recordset = dao.findById(textValue); 			break;
			case 2:recordset = dao.findByPhoneLike(textValue);		break;
			}
		}
		//mav.addObject("radioValue",radioValue);
		mav.addObject("recordSet", recordset);
		try {
			Thread.sleep(3000);//if here is not this,caused MySQL error.communication link failer.
		}catch(InterruptedException e) {e.printStackTrace();}
		return mav;
	}

	@RequestMapping(value="/delete_record",method=RequestMethod.GET)
	public ModelAndView deleteRecord(
			ModelAndView mav) {
		mav.setViewName("delete_record");
		M_StudentDao dao = new M_StudentDao(jdbc);
		Iterable<Student> list = dao.findAll();
		mav.addObject("recordSet", list);
		return mav;
	}
	@RequestMapping(value="/delete_record",method=RequestMethod.POST)
	public ModelAndView deleteRecordPost(
		@RequestParam("id")String id,
		ModelAndView mav) {

		return mav;
	}
	@RequestMapping(value="/delete_comfirm/{id}",method=RequestMethod.GET)
	public ModelAndView deleteComfirm(
			@PathVariable("id")String id,
			ModelAndView mav) {
		mav.setViewName("delete_comfirm");
		M_StudentDao dao = new M_StudentDao(jdbc);
		List<Student> recordset = dao.findById(id);
		mav.addObject("formModel",recordset.get(0));
		return mav;
	}
	@RequestMapping(value="/delete_comfirm",method=RequestMethod.POST)
	public ModelAndView deleteComfirmPost(
			@RequestParam("hiddenId")String id,
			ModelAndView mav) {
		mav.setViewName("index");
		M_StudentDao dao = new M_StudentDao(jdbc);
		dao.deleteById(id);
		try {
			Thread.sleep(3000);//if here is not this,caused MySQL error.communication link failer.
		}catch(InterruptedException e) {e.printStackTrace();}
		List<Student> recordset = dao.findAll();
		mav.addObject("recordSet", recordset);
		return mav;
	}
	@RequestMapping(value="/edit_form/{id}",method=RequestMethod.GET)
	public ModelAndView editForm(
			@PathVariable("id")String id,
			ModelAndView mav) {
		M_StudentDao dao = new M_StudentDao(jdbc);
		mav.setViewName("edit_form");
		List<Student> recordset = dao.findById(id);
		mav.addObject("formModel",recordset.get(0));
		return mav;
	}

	@RequestMapping(value="/edit_form",method=RequestMethod.POST)
	public ModelAndView editFormPost(
			@ModelAttribute("formModel")@Validated Student student,
			BindingResult result,
			ModelAndView mav) {
		M_StudentDao dao = new M_StudentDao(jdbc);
		dao.updateById(student);
		dao=null;
		mav.setViewName("index");
		List<Student> recordset = null;
		dao = new M_StudentDao(jdbc);
		recordset= dao.findAll();
		mav.addObject("order",1);
		mav.addObject("recordSet", recordset);
		return mav;
	}
	@RequestMapping(value="/edit_select",method=RequestMethod.GET)
	public ModelAndView editSelectGet(
			ModelAndView mav) {
		M_StudentDao dao = new M_StudentDao(jdbc);
		List<Student> recordset = dao.findAll();
		mav.addObject("recordSet", recordset);
		mav.setViewName("edit_select");
		return mav;
	}

    /**
     * This method downloads CSV file that contains all records form M_Students table. 
     */
    @RequestMapping("csvDownload")
    public void csvDownload(HttpServletResponse response) {
    	M_StudentDao dao = new M_StudentDao(jdbc);
    	CsvReader csvReader = new CsvReader();
    	csvReader.exportCSV(response, dao);
    }

}