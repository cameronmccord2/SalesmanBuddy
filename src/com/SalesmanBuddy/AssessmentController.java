package com.SalesmanBuddy;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.SalesmanBuddy.dao.SalesmanBuddyDAO;


@Controller
public class AssessmentController {
	@Inject
	@Named("salesmanBuddyDAO")
	private SalesmanBuddyDAO salesmanBuddyDAO;
	
	@RequestMapping(method=RequestMethod.GET, value="/health")
	public @ResponseBody int getAssessment(){
		return 1;
	}
	
//	@RequestMapping(method=RequestMethod.PUT, value="/assessment") //repeat
//	public @ResponseBody Assessment updateAssessment(@RequestBody Assessment newAssessment){
//		return langEvalDAO.updateAssessment(newAssessment);
//	}
//
//	@RequestMapping(method=RequestMethod.PUT, value="/assessment/item") //repeat
//	public @ResponseBody Item updateItem(@RequestBody Item item){
//		return langEvalDAO.updateItem(item);
//	}
//	
//	@RequestMapping(method=RequestMethod.GET, value="/assessment/{id}")
//	public @ResponseBody Assessment getAssessment(@PathVariable int id){
//		return langEvalDAO.getAssessment(id);
//	}
//	
//	@RequestMapping(method=RequestMethod.GET, value="/assessment/missionary/{id}") //done
//	public @ResponseBody Assessment getPartialAssessment(@PathVariable String id, @RequestParam(value="lang", required=true) int lang){
//		return langEvalDAO.getPartialAssessment(id,lang);
//	}
//	
//	@RequestMapping(method=RequestMethod.PUT, value="/assessment/item/handscore") //repeat
//	public @ResponseBody Item updateHandScore(@RequestBody HandScore handscore){
//		return langEvalDAO.updateHandScore(handscore);
//	}
//	
//	@RequestMapping(method=RequestMethod.GET, value="/assessments/{archived}")
//	public @ResponseBody List<Assessment> getAssessments(@PathVariable int archived){
//		return langEvalDAO.getAssessments(archived);
//	}
}